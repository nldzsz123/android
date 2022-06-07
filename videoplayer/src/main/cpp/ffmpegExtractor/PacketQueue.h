//
// Created by 飞拍科技 on 2017/5/24.
//

#ifndef ANDROIDSTUDY_PACKETQUEUE_H
#define ANDROIDSTUDY_PACKETQUEUE_H

#include <pthread.h>
#include <stdio.h>
#include <unistd.h>
#include <errno.h>
#include <stdlib.h>

typedef struct Frame
{
    int framesize;
    uint8_t *frameData;
}FrameData;

class PacketQueue {
private:
    FrameData* _queue;
    int _head;
    int _tail;
    int _size;
    int _count;

    pthread_mutex_t _mutex; // 互斥锁
    pthread_cond_t  _con;   // 条件变量
public:

    // 默认大小100
    PacketQueue(int bufferSize=100);

    virtual ~PacketQueue();

    bool addData(uint8_t* data, int size);

    uint8_t * pullData(int *size);

    int count(){ return _count;}
    void clear();

    void wakeUp();
};

#endif //ANDROIDSTUDY_PACKETQUEUE_H
