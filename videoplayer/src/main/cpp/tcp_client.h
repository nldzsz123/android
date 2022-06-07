//
// Created by 飞拍科技 on 2019/2/14.
//

#ifndef FLYPIEFORANDROID_TCP_CLIENT_H
#define FLYPIEFORANDROID_TCP_CLIENT_H
#ifdef __cplusplus
extern "C" {
#endif

int initSocket(const char *ip,const unsigned short port);
int connectToServer(int timeout);
int setIp(char * ip, int ipLen);
char* getRSSI();
int duiping(char * ssid, int ssidLen);

#ifdef __cplusplus
} // extern "C"
#endif
//void closeSocket();

#endif //FLYPIEFORANDROID_TCP_CLIENT_H
