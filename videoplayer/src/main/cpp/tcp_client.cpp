#include "tcp_client.h"
#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <stdlib.h>
#include <string.h>
#include <Boolean.hh>
#include <fcntl.h>
#include <asm-generic/fcntl.h>
#include <asm-generic/errno.h>
#include <errno.h>
#include "CPPLog.h"
#include "unistd.h"

static void crc_acc(unsigned char data, unsigned short *accum) {
    unsigned char temp;

    temp = data ^ (unsigned char) (*accum);
    temp ^= temp << 4;
    *accum = (*accum >> 8) ^ ((unsigned short) temp << 8) ^ ((unsigned short) temp << 3) ^
             ((unsigned short) temp >> 4);
}

unsigned short seq_crc(char *seq, unsigned char size) {
    unsigned short acc = 0xffff;

    while (size--)
        crc_acc(*seq++, &acc);

    crc_acc(0, &acc);

    return acc;
}

struct sockaddr_in their_addr;
int fd = 0;

int initSocket(const char *ip, const unsigned short port) {
    if (fd > 0) {
        close(fd);
        fd = 0;
    }

    if ((fd = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
        LOGE("create serial socket failed\n");
        return -1;
    }
    LOGE("initSocket %d", fd);
    memset(&their_addr, 0, sizeof(their_addr));
    their_addr.sin_family = AF_INET;
    their_addr.sin_port = htons(port);
    their_addr.sin_addr.s_addr = inet_addr(ip);
    return 0;
}

int connectToServer(int to) {
#ifdef SO_NOSIGPIPE
    int set_option = 1;
  setsockopt(socketNum, SOL_SOCKET, SO_NOSIGPIPE, &set_option, sizeof set_option);
#else
    signal(SIGPIPE, SIG_IGN);
#endif
    int flags = fcntl(fd, F_GETFL, 0);
    fcntl(fd, F_SETFL, flags | O_NONBLOCK);   // 设置为非阻塞模式
    int n = connect(fd, (struct sockaddr *) &their_addr, sizeof(struct sockaddr));
    if (n < 0) {  // 表示正在连接中 也认为失败,下次再继续连接
        if (errno == EINPROGRESS || errno == EWOULDBLOCK) {
            LOGE("errno %d", errno);
        }

        struct timeval tv;
        tv.tv_sec = to;
        tv.tv_usec = 0;
        fd_set wset;
        FD_ZERO(&wset);
        FD_SET(fd, &wset);
        n = select(fd + 1, NULL, &wset, NULL, &tv);
        if (n < 0) { // select出错
            LOGE("connect failed");
            close(fd);
            return -1;
        } else if (0 == n) { // 超时
            LOGE("connect timeout");
            close(fd);
            return -1;
        } else {  // 连接成功
            LOGE("select n %d", n);
        }
    }
    fcntl(fd, F_SETFL, flags & ~O_NONBLOCK);  // 设为阻塞模式
    struct timeval timeout = {to, 0};
    //设置发送超时
    setsockopt(fd, SOL_SOCKET, SO_SNDTIMEO, (char *) &timeout, sizeof(struct timeval));
    //设置接收超时
    setsockopt(fd, SOL_SOCKET, SO_RCVTIMEO, (char *) &timeout, sizeof(struct timeval));

    return 0;
}

int setIp(char *ip, int ipLen) {
    LOGE("ip %s len%d", ip, ipLen);
    char buffer[100] = {0x33,};
    *(buffer + 1) = ipLen;
    memcpy(buffer + 2, ip, ipLen);
    int crc = seq_crc(ip, ipLen);
    *(buffer + ipLen + 2) = (crc & 0xff);
    *(buffer + ipLen + 3) = (crc >> 8);
    ssize_t s = send(fd, buffer, ipLen + 4, MSG_WAITALL);
    if (s < 0) {
        LOGE("失败1");
        return errno;
    }

    uint8_t recevBuf[10];
    ssize_t len = recv(fd, recevBuf, 10, 0);
    if (len < 0) {
        LOGE("失败2");
        return -2;
    }

    if (recevBuf[0] == 0x33) {
        return 0;
    }
    LOGE("失败3");
    return -3;
}

char *getRSSI() {
    char buffer[100] = {0x34,};
    ssize_t s = send(fd, buffer, 2, MSG_WAITALL);
    if (s <= 0) {
        LOGE("值1==>%d", s);
        return NULL;
    }

    int len = 0;
    char *valid_str;
    int crc, crc_low, crc_high;
    memset(buffer, 0, 1);
    ssize_t t = recv(fd, buffer, sizeof(buffer), MSG_WAITALL);
    if (t <= 0) {
        LOGE("值2==>%d", t);
        return NULL;
    }
//    LOGE("值3==>%s",buffer);
    if (*buffer == 0x34) {
        len = *(buffer + 1);
        valid_str = (char *) malloc(len + 1);
        *(valid_str + len) = 0;
        memcpy(valid_str, buffer + 2, len);
//        LOGE("值==>%s",valid_str);
//        if ((uint8_t)crc != crc_low
//            ||  (uint8_t)(crc>>8) != crc_high) {
//            LOGE("crc error\n");
//            return NULL;
//        }
    }
    return valid_str;
}

int duiping(char *ssid, int ssidLen) {
    LOGE("ssid %s len %d", ssid, ssidLen);
    char buffer[100] = {0x32,};
    *(buffer + 1) = ssidLen;
    memcpy(buffer + 2, ssid, ssidLen);
    int crc = seq_crc(ssid, ssidLen);
    *(buffer + ssidLen + 2) = (crc & 0xff);
    *(buffer + ssidLen + 3) = (crc >> 8);
    ssize_t s = send(fd, buffer, ssidLen + 4, MSG_WAITALL);
    if (s < 0) {
        LOGE("失败1");
        return -1;
    }

    uint8_t recevBuf[10];
    ssize_t len = recv(fd, recevBuf, 10, 0);
    if (len < 0) {
        LOGE("失败2");
        return -1;
    }

    if (recevBuf[0] == 0x32) {
        return 0;
    }
    LOGE("失败3");
    return -1;
}

//void closeSocket(){
//    fd = -1;
//}
