package com.adventure.adventureaiagent.utils;

import cn.hutool.extra.pinyin.PinyinUtil;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.*;

public class HelloKryo {
   static public void main (String[] args) throws Exception {
      // 创建 Kryo 实例并注册类
      Kryo kryo = new Kryo();
      kryo.register(SomeClass.class);

      SomeClass object = new SomeClass();
      object.value = "Hello Kryo!";

      // 1序列化：将 SomeClass 对象写入 file.bin 文件
      Output output = new Output(new FileOutputStream("file.bin"));
      kryo.writeObject(output, object);
      output.close();

      // 2反序列化：从 file.bin 文件读取数据，还原为 SomeClass 对象
      Input input = new Input(new FileInputStream("file.bin"));
      SomeClass object2 = kryo.readObject(input, SomeClass.class);
      System.out.println(object2.value);
      input.close();

   }
   static public class SomeClass {
      String value;
   }
}