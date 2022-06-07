//
// Created by 飞拍科技 on 2019/3/2.
//

#include "Deocder.h"
#include "CPPLog.h"

Decoder::Decoder()
{
    LOGD("Decoder()");
}

Decoder::~Decoder()
{
    LOGD("~Decoder()");
}

void Decoder::setDecodeCallback(void *delegate,DecodeCallback *cb)
{
    fDelegate = delegate;
    fDecodeCallback = cb;
}