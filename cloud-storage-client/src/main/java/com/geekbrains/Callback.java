package com.geekbrains;

import com.geekgrains.common.Message;

import java.io.IOException;

public interface Callback {

    void callback(Message msg) throws IOException;

}
