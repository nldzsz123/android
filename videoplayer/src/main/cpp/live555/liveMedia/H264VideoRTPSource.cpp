/**********
This library is free software; you can redistribute it and/or modify it under
the terms of the GNU Lesser General Public License as published by the
Free Software Foundation; either version 2.1 of the License, or (at your
option) any later version. (See <http://www.gnu.org/copyleft/lesser.html>.)

This library is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
more details.

You should have received a copy of the GNU Lesser General Public License
along with this library; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
**********/
// "liveMedia"
// Copyright (c) 1996-2015 Live Networks, Inc.  All rights reserved.
// H.264 Video RTP Sources
// Implementation

#include "H264VideoRTPSource.hh"
#include "Base64.hh"

////////// H264BufferedPacket and H264BufferedPacketFactory //////////

class H264BufferedPacket: public BufferedPacket {
public:
  H264BufferedPacket(H264VideoRTPSource& ourSource);
  virtual ~H264BufferedPacket();

private: // redefined virtual functions
  virtual unsigned nextEnclosedFrameSize(unsigned char*& framePtr,
					 unsigned dataSize);
private:
  H264VideoRTPSource& fOurSource;
};

class H264BufferedPacketFactory: public BufferedPacketFactory {
private: // redefined virtual functions
  virtual BufferedPacket* createNewPacket(MultiFramedRTPSource* ourSource);
};


///////// H264VideoRTPSource implementation ////////

H264VideoRTPSource*
H264VideoRTPSource::createNew(UsageEnvironment& env, Groupsock* RTPgs,
			      unsigned char rtpPayloadFormat,
			      unsigned rtpTimestampFrequency) {
  return new H264VideoRTPSource(env, RTPgs, rtpPayloadFormat,
				rtpTimestampFrequency);
}

H264VideoRTPSource
::H264VideoRTPSource(UsageEnvironment& env, Groupsock* RTPgs,
		     unsigned char rtpPayloadFormat,
		     unsigned rtpTimestampFrequency)
  : MultiFramedRTPSource(env, RTPgs, rtpPayloadFormat, rtpTimestampFrequency,
			 new H264BufferedPacketFactory) {
}

H264VideoRTPSource::~H264VideoRTPSource() {
}

Boolean H264VideoRTPSource
::processSpecialHeader(BufferedPacket* packet,
                       unsigned& resultSpecialHeaderSize) {
  unsigned char* headerStart = packet->data();
  unsigned packetSize = packet->dataSize();
  unsigned numBytesToSkip;

  // Check the 'nal_unit_type' for special 'aggregation' or 'fragmentation' packets:
  if (packetSize < 1) return False;
  fCurPacketNALUnitType = (headerStart[0]&0x1F);
  int nalu_type = fCurPacketNALUnitType;
  unsigned char startBit = 0;
  unsigned char endBit = 0;
//    printf("processSpecialHeader %d\n",fCurPacketNALUnitType);
  switch (fCurPacketNALUnitType) {
    case 24: { // STAP-A
      numBytesToSkip = 1; // discard the type byte
      break;
    }
    case 25: case 26: case 27: { // STAP-B, MTAP16, or MTAP24
      numBytesToSkip = 3; // discard the type byte, and the initial DON
      break;
    }
    case 28: case 29: { // // FU-A or FU-B
      // For these NALUs, the first two bytes are the FU indicator and the FU header.
      // If the start bit is set, we reconstruct the original NAL header into byte 1:
      if (packetSize < 2) return False;
//            unsigned char startBit = headerStart[1]&0x80;   // 第二个字节的第1bit为1代表一帧的开始
//            unsigned char endBit = headerStart[1]&0x40;     // 第二个字节的第2bit为1代表一帧的结束
      startBit = headerStart[1]&0x80;
      endBit = headerStart[1]&0x40;
      if (startBit) {
//                fCurrentPacketBeginsFrame = True;
        // 求得第二个字节的NALU type，把FU-A的第一个包第一个字节拿掉
        headerStart[1] = (headerStart[0]&0xE0)|(headerStart[1]&0x1F);
        numBytesToSkip = 1;
      } else {
        // The start bit is not set, so we skip both the FU indicator and header:
        // 剩余的FU-A分片组合在一起，所以这里要拿掉前面两个字节
//                fCurrentPacketBeginsFrame = False;
        numBytesToSkip = 2;
      }
//            fCurrentPacketCompletesFrame = (endBit != 0);


      nalu_type = headerStart[1]&0x1F;
//            printf("processSpecialHeader1111 %d begin %d end %d seq %d type %d\n",fCurPacketNALUnitType,startBit,endBit,packet->rtpSeqNo(), headerStart[1]&0x1F);
      break;
    }
    default: {
      // This packet contains one complete NAL unit:
//            fCurrentPacketBeginsFrame = fCurrentPacketCompletesFrame = True;
      startBit = 1;
      endBit =1;
      numBytesToSkip = 0;
      break;
    }
  }
  resultSpecialHeaderSize = numBytesToSkip;

  if (nalu_type == 7 || nalu_type == 8) {
    if (nalu_type == 7) {
      h264_decode_seq_parameter_set_out(headerStart, packetSize, &_sps_w, &_sps_h, &_sps_fps, &_currentSPS);
    }
    fCurrentPacketBeginsFrame = fCurrentPacketCompletesFrame = True;
  } else if (nalu_type == 1 || nalu_type == 5) {

    if (startBit) {
      H264SliceHeaderSimpleInfo info;
      info.frame_num = -1;
      info.slice_type = -1;
      info.first_mb_in_slice = -1;
      h264_decode_slice_header(headerStart+numBytesToSkip+1, packetSize-numBytesToSkip-1, &_currentSPS, &info);
      if (info.first_mb_in_slice == 0 && _lastMarkBit == -1) {
        fCurrentPacketBeginsFrame = True;
        _lastMarkBit = packet->rtpMarkerBit();
      } else {
        fCurrentPacketBeginsFrame = False;
      }
    } else {
      fCurrentPacketBeginsFrame = False;
    }

    fCurrentPacketCompletesFrame = packet->rtpMarkerBit();
    if (fCurrentPacketCompletesFrame) {
      _lastMarkBit = -1;
      fCurrentPacketBeginsFrame = False;
    }

    if (endBit && !fCurrentPacketCompletesFrame) {
      unsigned char startCode[4];
      startCode[0] = 0;
      startCode[1] = 0;
      startCode[2] = 0;
      startCode[3] = 1;
      packet->appendData(startCode, 4);
    }
  }

//    printf("processSpecialHeader type %d size %d mark %d start %d end %d seq %d\n",nalu_type,packet->dataSize(),packet->rtpMarkerBit(),startBit,endBit,packet->rtpSeqNo());
  return True;
}

char const* H264VideoRTPSource::MIMEtype() const {
  return "video/H264";
}

SPropRecord* parseSPropParameterSets(char const* sPropParameterSetsStr,
                                     // result parameter:
                                     unsigned& numSPropRecords) {
  // Make a copy of the input string, so we can replace the commas with '\0's:
  char* inStr = strDup(sPropParameterSetsStr);
  if (inStr == NULL) {
    numSPropRecords = 0;
    return NULL;
  }

  // Count the number of commas (and thus the number of parameter sets):
  numSPropRecords = 1;
  char* s;
  for (s = inStr; *s != '\0'; ++s) {
    if (*s == ',') {
      ++numSPropRecords;
      *s = '\0';
    }
  }

  // Allocate and fill in the result array:
  SPropRecord* resultArray = new SPropRecord[numSPropRecords];
  s = inStr;
  for (unsigned i = 0; i < numSPropRecords; ++i) {
    resultArray[i].sPropBytes = base64Decode(s, resultArray[i].sPropLength);
    s += strlen(s) + 1;
  }

  delete[] inStr;
  return resultArray;
}


////////// H264BufferedPacket and H264BufferedPacketFactory implementation //////////

H264BufferedPacket::H264BufferedPacket(H264VideoRTPSource& ourSource)
  : fOurSource(ourSource) {
}

H264BufferedPacket::~H264BufferedPacket() {
}

unsigned H264BufferedPacket
::nextEnclosedFrameSize(unsigned char*& framePtr, unsigned dataSize) {
  unsigned resultNALUSize = 0; // if an error occurs

  switch (fOurSource.fCurPacketNALUnitType) {
  case 24: case 25: { // STAP-A or STAP-B
    // The first two bytes are NALU size:
    if (dataSize < 2) break;
    resultNALUSize = (framePtr[0]<<8)|framePtr[1];
    framePtr += 2;
    break;
  }
  case 26: { // MTAP16
    // The first two bytes are NALU size.  The next three are the DOND and TS offset:
    if (dataSize < 5) break;
    resultNALUSize = (framePtr[0]<<8)|framePtr[1];
    framePtr += 5;
    break;
  }
  case 27: { // MTAP24
    // The first two bytes are NALU size.  The next four are the DOND and TS offset:
    if (dataSize < 6) break;
    resultNALUSize = (framePtr[0]<<8)|framePtr[1];
    framePtr += 6;
    break;
  }
  default: {
    // Common case: We use the entire packet data:
    return dataSize;
  }
  }

  return (resultNALUSize <= dataSize) ? resultNALUSize : dataSize;
}

BufferedPacket* H264BufferedPacketFactory
::createNewPacket(MultiFramedRTPSource* ourSource) {
  return new H264BufferedPacket((H264VideoRTPSource&)(*ourSource));
}
