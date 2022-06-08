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
// A generic RTSP client - for a single "rtsp://" URL
// C++ header

#ifndef _RTSP_CLIENT_HH
#define _RTSP_CLIENT_HH

#ifndef _MEDIA_SESSION_HH
#include "MediaSession.hh"
#endif
#ifndef _NET_ADDRESS_HH
#include "NetAddress.hh"
#endif
#ifndef _DIGEST_AUTHENTICATION_HH
#include "DigestAuthentication.hh"
#endif

class RTSPClient: public Medium {
public:
  static RTSPClient* createNew(UsageEnvironment& env, char const* rtspURL,
			       int verbosityLevel = 0,
			       char const* applicationName = NULL,
			       int socketNumToServer = -1);
  // If "tunnelOverHTTPPortNum" is non-zero, we tunnel RTSP (and RTP)
  //     over a HTTP connection with the given port number, using the technique
  //     described in Apple's document <http://developer.apple.com/documentation/QuickTime/QTSS/Concepts/chapter_2_section_14.html>
  // If "socketNumToServer" is >= 0, then it is the socket number of an already-existing TCP connection to the server.
  //     (In this case, "rtspURL" must point to the socket's endpoint, so that it can be accessed via the socket.)

  typedef void (responseHandler)(RTSPClient* rtspClient,
				 int resultCode, char* resultString);
      // A function that is called in response to a RTSP command.  The parameters are as follows:
      //     "rtspClient": The "RTSPClient" object on which the original command was issued.
      //     "resultCode": If zero, then the command completed successfully.  If non-zero, then the command did not complete
      //         successfully, and "resultCode" indicates the error, as follows:
      //             A positive "resultCode" is a RTSP error code (for example, 404 means "not found")
      //             A negative "resultCode" indicates a socket/network error; 0-"resultCode" is the standard "errno" code.
      //     "resultString": A ('\0'-terminated) string returned along with the response, or else NULL.
      //         In particular:
      //             "resultString" for a successful "DESCRIBE" command will be the media session's SDP description.
      //             "resultString" for a successful "OPTIONS" command will be a list of allowed commands.
      //         Note that this string can be present (i.e., not NULL) even if "resultCode" is non-zero - i.e., an error message.
      //         Also, "resultString" can be NULL, even if "resultCode" is zero (e.g., if the RTSP command succeeded, but without
      //             including an appropriate result header).
      //         Note also that this string is dynamically allocated, and must be freed by the handler (or the caller)
      //             - using "delete[]".

  unsigned sendDescribeCommand(responseHandler* responseHandler, Authenticator* authenticator = NULL);
      // Issues a RTSP "DESCRIBE" command, then returns the "CSeq" sequence number that was used in the command.
      // The (programmer-supplied) "responseHandler" function is called later to handle the response
      //     (or is called immediately - with an error code - if the command cannot be sent).
      // "authenticator" (optional) is used for access control.  If you have username and password strings, you can use this by
      //     passing an actual parameter that you created by creating an "Authenticator(username, password) object".
      //     (Note that if you supply a non-NULL "authenticator" parameter, you need do this only for the first command you send.)

  unsigned sendOptionsCommand(responseHandler* responseHandler, Authenticator* authenticator = NULL);
      // Issues a RTSP "OPTIONS" command, then returns the "CSeq" sequence number that was used in the command.
      // (The "responseHandler" and "authenticator" parameters are as described for "sendDescribeCommand".)

  unsigned sendAnnounceCommand(char const* sdpDescription, responseHandler* responseHandler, Authenticator* authenticator = NULL);
      // Issues a RTSP "ANNOUNCE" command (with "sdpDescription" as parameter),
      //     then returns the "CSeq" sequence number that was used in the command.
      // (The "responseHandler" and "authenticator" parameters are as described for "sendDescribeCommand".)

  unsigned sendSetupCommand(MediaSubsession& subsession, responseHandler* responseHandler,
			    Boolean streamOutgoing = False,
			    Boolean streamUsingTCP = False,
			    Boolean forceMulticastOnUnspecified = False,
			    Authenticator* authenticator = NULL);
      // Issues a RTSP "SETUP" command, then returns the "CSeq" sequence number that was used in the command.
      // (The "responseHandler" and "authenticator" parameters are as described for "sendDescribeCommand".)

  unsigned sendPlayCommand(MediaSession& session, responseHandler* responseHandler,
			   double start = 0.0f, double end = -1.0f, float scale = 1.0f,
			   Authenticator* authenticator = NULL);
      // Issues an aggregate RTSP "PLAY" command on "session", then returns the "CSeq" sequence number that was used in the command.
      // (Note: start=-1 means 'resume'; end=-1 means 'play to end')
      // (The "responseHandler" and "authenticator" parameters are as described for "sendDescribeCommand".)
  unsigned sendPlayCommand(MediaSubsession& subsession, responseHandler* responseHandler,
			   double start = 0.0f, double end = -1.0f, float scale = 1.0f,
			   Authenticator* authenticator = NULL);
      // Issues a RTSP "PLAY" command on "subsession", then returns the "CSeq" sequence number that was used in the command.
      // (Note: start=-1 means 'resume'; end=-1 means 'play to end')
      // (The "responseHandler" and "authenticator" parameters are as described for "sendDescribeCommand".)

  // Alternative forms of "sendPlayCommand()", used to send "PLAY" commands that include an 'absolute' time range:
  // (The "absStartTime" string (and "absEndTime" string, if present) *must* be of the form
  //  "YYYYMMDDTHHMMSSZ" or "YYYYMMDDTHHMMSS.<frac>Z")
  unsigned sendPlayCommand(MediaSession& session, responseHandler* responseHandler,
			   char const* absStartTime, char const* absEndTime = NULL, float scale = 1.0f,
			   Authenticator* authenticator = NULL);
  unsigned sendPlayCommand(MediaSubsession& subsession, responseHandler* responseHandler,
			   char const* absStartTime, char const* absEndTime = NULL, float scale = 1.0f,
			   Authenticator* authenticator = NULL);

  unsigned sendPauseCommand(MediaSession& session, responseHandler* responseHandler, Authenticator* authenticator = NULL);
      // Issues an aggregate RTSP "PAUSE" command on "session", then returns the "CSeq" sequence number that was used in the command.
      // (The "responseHandler" and "authenticator" parameters are as described for "sendDescribeCommand".)
  unsigned sendPauseCommand(MediaSubsession& subsession, responseHandler* responseHandler, Authenticator* authenticator = NULL);
      // Issues a RTSP "PAUSE" command on "subsession", then returns the "CSeq" sequence number that was used in the command.
      // (The "responseHandler" and "authenticator" parameters are as described for "sendDescribeCommand".)

  unsigned sendRecordCommand(MediaSession& session, responseHandler* responseHandler, Authenticator* authenticator = NULL);
      // Issues an aggregate RTSP "RECORD" command on "session", then returns the "CSeq" sequence number that was used in the command.
      // (The "responseHandler" and "authenticator" parameters are as described for "sendDescribeCommand".)
  unsigned sendRecordCommand(MediaSubsession& subsession, responseHandler* responseHandler, Authenticator* authenticator = NULL);
      // Issues a RTSP "RECORD" command on "subsession", then returns the "CSeq" sequence number that was used in the command.
      // (The "responseHandler" and "authenticator" parameters are as described for "sendDescribeCommand".)

  unsigned sendTeardownCommand(MediaSession& session, responseHandler* responseHandler, Authenticator* authenticator = NULL);
      // Issues an aggregate RTSP "TEARDOWN" command on "session", then returns the "CSeq" sequence number that was used in the command.
      // (The "responseHandler" and "authenticator" parameters are as described for "sendDescribeCommand".)
  unsigned sendTeardownCommand(MediaSubsession& subsession, responseHandler* responseHandler, Authenticator* authenticator = NULL);
      // Issues a RTSP "TEARDOWN" command on "subsession", then returns the "CSeq" sequence number that was used in the command.
      // (The "responseHandler" and "authenticator" parameters are as described for "sendDescribeCommand".)

  unsigned sendSetParameterCommand(MediaSession& session, responseHandler* responseHandler,
				   char const* parameterName, char const* parameterValue,
				   Authenticator* authenticator = NULL);
      // Issues an aggregate RTSP "SET_PARAMETER" command on "session", then returns the "CSeq" sequence number that was used in the command.
      // (The "responseHandler" and "authenticator" parameters are as described for "sendDescribeCommand".)

  unsigned sendGetParameterCommand(MediaSession& session, responseHandler* responseHandler, char const* parameterName,
				   Authenticator* authenticator = NULL);
      // Issues an aggregate RTSP "GET_PARAMETER" command on "session", then returns the "CSeq" sequence number that was used in the command.
      // (The "responseHandler" and "authenticator" parameters are as described for "sendDescribeCommand".)

  void sendDummyUDPPackets(MediaSession& session, unsigned numDummyPackets = 2);
  void sendDummyUDPPackets(MediaSubsession& subsession, unsigned numDummyPackets = 2);
      // Sends short 'dummy' (i.e., non-RTP or RTCP) UDP packets towards the server, to increase
      // the likelihood of RTP/RTCP packets from the server reaching us if we're behind a NAT.
      // (If we requested RTP-over-TCP streaming, then these functions have no effect.)
      // Our implementation automatically does this just prior to sending each "PLAY" command;
      // You should not call these functions yourself unless you know what you're doing.

  Boolean changeResponseHandler(unsigned cseq, responseHandler* newResponseHandler);
      // Changes the response handler for the previously-performed command (whose operation returned "cseq").
      // (To turn off any response handling for the command, use a "newResponseHandler" value of NULL.  This might be done as part
      //  of an implementation of a 'timeout handler' on the command, for example.)
      // This function returns True iff "cseq" was for a valid previously-performed command (whose response is still unhandled).

  int socketNum() const { return fInputSocketNum; }

  static Boolean lookupByName(UsageEnvironment& env,
			      char const* sourceName,
			      RTSPClient*& resultClient);

  static Boolean parseRTSPURL(UsageEnvironment& env, char const* url,
			      char*& username, char*& password, NetAddress& address, portNumBits& portNum, char const** urlSuffix = NULL);
      // Parses "url" as "rtsp://[<username>[:<password>]@]<server-address-or-name>[:<port>][/<stream-name>]"
      // (Note that the returned "username" and "password" are either NULL, or heap-allocated strings that the caller must later delete[].)

  void setUserAgentString(char const* userAgentName);
      // sets an alternative string to be used in RTSP "User-Agent:" headers

  void disallowBasicAuthentication() { fAllowBasicAuthentication = False; }
      // call this if you don't want the server to request 'Basic' authentication
      // (which would cause the client to send usernames and passwords over the net).

  unsigned sessionTimeoutParameter() const { return fSessionTimeoutParameter; }

  char const* url() const { return fBaseURL; }

  static unsigned responseBufferSize;

public: // Some compilers complain if this is "private:"
  // The state of a request-in-progress:
  // 表示了一个请求记录节点，多个请求记录节点将构成一个请求队列。该节点包含了序列号，请求的命令名，回调函数，session subsession等变量，有两个构造函数，分别用于一般的请求和play时的请求。
  class RequestRecord {
  public:
    RequestRecord(unsigned cseq, char const* commandName, responseHandler* handler,
		  MediaSession* session = NULL, MediaSubsession* subsession = NULL, u_int32_t booleanFlags = 0,
		  double start = 0.0f, double end = -1.0f, float scale = 1.0f, char const* contentStr = NULL);
    RequestRecord(unsigned cseq, responseHandler* handler,
		  char const* absStartTime, char const* absEndTime = NULL, float scale = 1.0f,
		  MediaSession* session = NULL, MediaSubsession* subsession = NULL);
        // alternative constructor for creating "PLAY" requests that include 'absolute' time values
    virtual ~RequestRecord();

    RequestRecord*& next() { return fNext; }
    unsigned& cseq() { return fCSeq; }
    char const* commandName() const { return fCommandName; }
    MediaSession* session() const { return fSession; }
    MediaSubsession* subsession() const { return fSubsession; }
    u_int32_t booleanFlags() const { return fBooleanFlags; }
    double start() const { return fStart; }
    double end() const { return fEnd; }
    char const* absStartTime() const { return fAbsStartTime; }
    char const* absEndTime() const { return fAbsEndTime; }
    float scale() const { return fScale; }
    char* contentStr() const { return fContentStr; }
    responseHandler*& handler() { return fHandler; }

  private:
    RequestRecord* fNext;
    unsigned fCSeq;
    char const* fCommandName;
    MediaSession* fSession;
    MediaSubsession* fSubsession;
    u_int32_t fBooleanFlags;
    double fStart, fEnd;
    char *fAbsStartTime, *fAbsEndTime; // used for optional 'absolute' (i.e., "time=") range specifications
    float fScale;
    char* fContentStr;
    responseHandler* fHandler;
  };

protected:
  RTSPClient(UsageEnvironment& env, char const* rtspURL,
	     int verbosityLevel, char const* applicationName, int socketNumToServer);
      // called only by createNew();
  virtual ~RTSPClient();

  void reset();
  void setBaseURL(char const* url);
  int grabSocket(); // allows a subclass to reuse our input socket, so that it won't get closed when we're deleted
  virtual unsigned sendRequest(RequestRecord* request);
  virtual Boolean setRequestFields(RequestRecord* request,
				   char*& cmdURL, Boolean& cmdURLWasAllocated,
				   char const*& protocolStr,
				   char*& extraHeaders, Boolean& extraHeadersWereAllocated);
      // used to implement "sendRequest()"; subclasses may reimplement this (e.g., when implementing a new command name)

private: // redefined virtual functions
  virtual Boolean isRTSPClient() const;

private:
  // 专门用于存储前面请求记录节点的请求记录队列，包含了对节点操作的增enqueue，删，该，查操作。
  class RequestQueue {
  public:
    RequestQueue();
    RequestQueue(RequestQueue& origQueue); // moves the queue contents to the new queue
    virtual ~RequestQueue();
    
    // 将记录放在链表的末尾。
    void enqueue(RequestRecord* request); // "request" must not be NULL
    // 弹出最前面的一个节点。
    RequestRecord* dequeue();
    // 将记录放在链表的最开头
    void putAtHead(RequestRecord* request); // "request" must not be NULL
    // 根据序列号查找一个节点
    RequestRecord* findByCSeq(unsigned cseq);
    // 判断链表是否为空
    Boolean isEmpty() const { return fHead == NULL; }

  // 链表包含一个指向第一个节点和指向最后一个节点的指针，他们默认为NULL
  private:
    RequestRecord* fHead;
    RequestRecord* fTail;
  };

  void resetTCPSockets();
  void resetResponseBuffer();
  int openConnection(); // -1: failure; 0: pending; 1: success
  int connectToServer(int socketNum, portNumBits remotePortNum); // used to implement "openConnection()"; result values are the same
  char* createAuthenticatorString(char const* cmd, char const* url);
  void handleRequestError(RequestRecord* request);
  Boolean parseResponseCode(char const* line, unsigned& responseCode, char const*& responseString);
  void handleIncomingRequest();
  static Boolean checkForHeader(char const* line, char const* headerName, unsigned headerNameLength, char const*& headerParams);
  Boolean parseTransportParams(char const* paramsStr,
			       char*& serverAddressStr, portNumBits& serverPortNum,
			       unsigned char& rtpChannelId, unsigned char& rtcpChannelId);
  Boolean parseScaleParam(char const* paramStr, float& scale);
  Boolean parseRTPInfoParams(char const*& paramStr, u_int16_t& seqNum, u_int32_t& timestamp);
  Boolean handleSETUPResponse(MediaSubsession& subsession, char const* sessionParamsStr, char const* transportParamsStr,
			      Boolean streamUsingTCP);
  Boolean handlePLAYResponse(MediaSession& session, MediaSubsession& subsession,
                             char const* scaleParamsStr, char const* rangeParamsStr, char const* rtpInfoParamsStr);
  Boolean handleTEARDOWNResponse(MediaSession& session, MediaSubsession& subsession);
  Boolean handleGET_PARAMETERResponse(char const* parameterName, char*& resultValueString);
  Boolean handleAuthenticationFailure(char const* wwwAuthenticateParamsStr);
  Boolean resendCommand(RequestRecord* request);
  char const* sessionURL(MediaSession const& session) const;
  static void handleAlternativeRequestByte(void*, u_int8_t requestByte);
  void handleAlternativeRequestByte1(u_int8_t requestByte);
  void constructSubsessionURL(MediaSubsession const& subsession,
			      char const*& prefix,
			      char const*& separator,
			      char const*& suffix);

  // Support for asynchronous connections to the server:
  static void connectionHandler(void*, int /*mask*/);
  void connectionHandler1();

  // Support for handling data sent back by a server:
  static void incomingDataHandler(void*, int /*mask*/);
  void incomingDataHandler1();
  void handleResponseBytes(int newBytesRead);

protected:
  // 用于打印日志用的日志级别变量
  int fVerbosityLevel;
  // 用于记录发送的所有命令请求RequestRecord的序列号 每发一个增加1
  unsigned fCSeq; // sequence number, used in consecutive requests
  // 用于RTSP协议或者HTTP协议交互认证用的证书。RTSP协议认证有两种方式 基本认证(basic authentication)和摘要认证(digest authentication)，前者安全性较差。
  Authenticator fCurrentAuthenticator;
  Boolean fAllowBasicAuthentication;
  // 服务器的ip地址
  netAddressBits fServerAddress;

private:
  char* fUserAgentHeaderStr;
  unsigned fUserAgentHeaderStrLen;
  // 用于收发数据的 socket，这里收发数据都使用同一个socket。
  int fInputSocketNum, fOutputSocketNum;
  char* fBaseURL;
  unsigned char fTCPStreamIdCount; // used for (optional) RTP/TCP
  char* fLastSessionId;
  unsigned fSessionTimeoutParameter; // optionally set in response "Session:" headers
  char* fResponseBuffer;
  unsigned fResponseBytesAlreadySeen, fResponseBufferBytesLeft;
  // 两个队列，一个是消息请求队列，一个是消息响应队列。工作流程是，所有的消息先放入消息请求队列，发送一个消息，然后将该消息放入消息响应队列，得到消息的回应后，再将该消息从消息响应队列中移除。
  RequestQueue fRequestsAwaitingConnection, fRequestsAwaitingResponse;
};
#endif