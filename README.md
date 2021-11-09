# P4-and-ONOS-development

本项目适合具有一些P4语言以及ONOS基础，并想继续结合P4与ONOS进行开发的人员。

## 软件功能介绍

- 二层转发
- 三层转发
- 组播

## 运行环境

| Software            | Description                                                                          |
|---------------------|------------------------------------------------------------------------------------- |
| Ubuntu              | 16.04 Desktop LTS(kerner 4.15.0)                                                     |
| P4                  | Refer to the website https://blog.csdn.net/father_is_/article/details/108225712      |
| ONOS                | Refer to the website https://wiki.onosproject.org/display/ONOS/Developer+Quick+Start |
| Maven               | Apache Maven 3.3.9                                                                   |
| Java                | Version:11.0.10                                                                      |

在安装P4与ONOS的过程中可能会出现一些问题，最大的原因可能是相关文件下载不了。所以我们在安装软件之前，尽量保持在网速较快的
情况下，或者通过"梯子"去进行下载安装。

## 使用方法

**1. 编译P4代码**
  
    cd ipv4/pipeconf/src/main/resources/
    p4c-bm2-ss  --p4v 16 --arch v1model -o ipv4.json --p4runtime-files ipv4.p4info.txt --Wdisable=unsupported ./ipv4.p4
  
**2. 编译ONOS配置文件pipeconf**

再开一个终端输入：

    cd ipv4/pipeconf/
    mvn clean package
  
最后会生成一个我们所需要的onos-apps-ipv4-pipeconf.oar。
  
**3. 编译ONOS下发流表文件ipv4**

再开一个终端输入：

    cd ipv4/ipv4/
    mvn clean package  

最后会生成一个我们所需要的onos-apps-ipv4-ipv4.oar。

**4. 运行ONOS**

再开一个终端输入：

    cd onos/
    bazel run onos-local -- clean debug  

**5. 在ONOS应用中加入编译后的pipeconf与ipv4**

打开网址：<http://localhost:8181/onos/ui>;

登录的用户名为*onos*，密码为*rocks*；

将onos-apps-ipv4-ipv4.oar和onos-apps-ipv4-pipeconf.oar导入onos控制器。

![image](https://user-images.githubusercontent.com/67526535/140602787-1d414fa6-293c-4b58-8f49-96ff10f07ae5.png)

先点击应用，再点击箭头，激活应用。应用激活顺序：
BMV2，ipv4-pipeconf(注：IPV4-DEMO-APP先不要激活)。

![image](https://user-images.githubusercontent.com/67526535/140602853-c73ce19c-ac79-4325-98be-bcf2ac2cc590.png)
  
**6. 创建拓扑**

将本项目中的bmv2.py替代文件onos/tools/dev/mininet/bmv2.py，再开一个终端输入：

    export BMV2_MN_PY="/home/qt/onos/tools/dev/mininet/bmv2.py"
    sudo -E mn  --custom $BMV2_MN_PY --switch onosbmv2,pipeconf=nextworking-ipv4-pipeconf --topo mytopo --controller remote,ip=127.0.0.1 
  
参数说明：

-E：载入环境变量值；

--custom：用户自定义拓扑，其中$BMV2_MN_PY指的是home/qt/onos/tools/dev/mininet/bmv2.py；

--switch: 设置的交换机类型，pipeconf=nextworking-ipv4-pipeconf是指定netcfg的pipeconf类型；

--topo: mytopo是bmv2.py文件结尾的自定义的topo，可参考更改;

--controller: remote,ip=127.0.0.1是连接控制器的地址，本地默认127.0.0.1。

![image](https://user-images.githubusercontent.com/67526535/140698468-846546f7-1432-4fad-a031-31a3893cea6e.png)

**7. 激活IPV4-DEMO-APP**

跟步骤5一样的操作来激活IPV4-DEMO-APP；

打开网页界面，点击最左上角的图标选择拓扑。此时我们可以看到我们创建好的拓扑图，按"L"键可以显示设备名称。点击交换机设备，会弹出一个设
备介绍框，设备中的流表等信息都可以点击相应的按钮进行查看；

在Mininet shell 上输入：

    pingall
  
可以看到：

![image](https://user-images.githubusercontent.com/67526535/140603321-3b408990-60f8-4dc1-ae75-4acddc276997.png)

至此实验结束。

## 结束语

  本次实验结合了二层转发、三层转发以及组播的功能，如果读者有任何疑问或者想进一步开发的，欢迎随时讨论。
