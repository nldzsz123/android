//
// Created by 飞拍科技 on 2017/5/24.
//

#include "PacketQueue.h"
#include "CPPLog.h"

PacketQueue::PacketQueue(int size):_size(size)
{
    _head = 0;
    _tail = 0;
    _count = 0;
    _size = size;

    pthread_mutex_init(&_mutex, NULL);  // 第二个参数为NULL 则是普通的互斥锁
    pthread_cond_init(&_con, NULL);
//    _queue = (FrameData*)malloc(sizeof(FrameData) * size);
    _queue = new FrameData[size];
}
PacketQueue::~PacketQueue()
{
    _head = 0;
    _tail = 0;
    _count = 0;
    _size = 0;

    if (_queue != NULL) {
        delete _queue;
    }

    pthread_mutex_destroy(&_mutex);
    pthread_cond_destroy(&_con);
}

bool PacketQueue::addData(uint8_t *data, int length)
{
    pthread_mutex_lock(&_mutex);    // 枷锁
    if (data == NULL || length <= 0 || _count >= _size) {
        // 要存储的数据不合法或者队列已满 则丢弃该数据
        if (data != NULL && length > 0) {
            free(data);
        }
        pthread_mutex_unlock(&_mutex);  // 解锁
        return false;
    }
    // 不停的往_tail所指的位置添加数据
    _queue[_tail].frameData = data;
    _queue[_tail].framesize = length;
    _tail++;    // 位置后移，下一个数据将放在这里
    if (_tail >= _size) {   // 如果_tail指向的位置到达末尾了，则从头开始
        _tail = 0;
    }

    _count++;
    pthread_cond_signal(&_con); // 发出条件变量，唤醒正在等待的枷锁线程
    pthread_mutex_unlock(&_mutex);
    return true;
}

uint8_t * PacketQueue::pullData(int *length)
{
    pthread_mutex_lock(&_mutex);
    // 判断队列是否为空
    if (_count <= 0) {
        struct timeval tv;
        gettimeofday(&tv, NULL);    // 获取当前默认的系统时钟
        struct timespec ts;
        ts.tv_sec = tv.tv_sec + 2;
        ts.tv_nsec = tv.tv_usec * 1000;
        // 为空则进入等待状态，进入等待后，锁可以被其它线程再次上锁
        pthread_cond_timedwait(&_con, &_mutex, &ts);
        if (_count == 0) {
            *length = 0;
            pthread_mutex_unlock(&_mutex);
            return NULL;
        }
    }

    // 取出_head所指向的位置的数据
    uint8_t* tmp = NULL;
    tmp = _queue[_head].frameData;
    *length = _queue[_head].framesize;
    _head++;    // 位置后移，下一个要取的数据的位置
    if (_head >= _size) {   // 如果到达末尾了，则从头开始取
        _head = 0;
    }
    _count--;

    pthread_mutex_unlock(&_mutex);
    return tmp;
}

void PacketQueue::clear()
{
    pthread_mutex_lock(&_mutex);
    int idx = 0;
    for (int i=0; i<_count; i++) {
        if (i+_head >= _size) {
            idx = i+_head-_size;
        } else {
            idx = i + _head;
        }
        if (_queue[idx].frameData != NULL) {
            _queue[idx].frameData = NULL;
            _queue[idx].framesize = 0;
        }
    }
    _head = 0;
    _tail = 0;
    _count = 0;
    pthread_mutex_unlock(&_mutex);
}

void PacketQueue::wakeUp()
{
    pthread_mutex_lock(&_mutex);
    pthread_cond_signal(&_con);
    pthread_mutex_unlock(&_mutex);
}
