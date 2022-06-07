package com.feipai.flypai.utils.socket;

import com.feipai.flypai.beans.FileBean;

import java.util.List;

public interface DataSocketReadListener extends TcpReadListener {


    void read(List<FileBean> fbs, int index, byte[] buffer);


    void uploadLisenter(int progress);
}
