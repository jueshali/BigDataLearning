# Xshell 学习

学习目的主要是用于写一些大数据的脚本

## 第一个shell脚本

```bash
#!/bin/bash       告诉别人该程序用bash执行
echo hello
```

```python
bash first.sh
```

### 编写

以#！/bin/bash做为脚本说明，文件中需要编写可以执行的脚本命令

### 执行

- 通过使用bash直接执行shell脚本，会新开一个bash,与原有bash中的变量无关。
- 使用./也可以执行，需要root权限~
- soure first.sh
- .first.sh

## 变量

### 系统变量

$HOME, $PWD, $SHELL

### 自定义变量

#### 基本语法

1. 定义变量：变量=值
2. 撤销变量：unset 变量
3. 声明静态变量，readonly变量。

#### 变量定义规则

1. 字母下划线开头，由字母数字下划线组成。环境变量要大写
2. 等号两侧不能为空。
3. 在bash中，变量类型默认为字符型，无法直接进行数值运算。
4. 变量的值如果有空格，需要用双引号或者单引号括起来。i="hello $a"
5. 使用双引号，可以识别空格以外的其他变量。i="hello $a"
6. 使用反引号，可以将语句运行的结果作为值赋值给变量。
7. set | grep i= 可以查询i
8. 当bash关闭时，例如断开连接，当前bash已经定义的变量全部消失。
9. export，将当前变量提升为全局变量。在新开的bash中也可以使用全局变量，只要全局变量所在的bash不关闭，那么所有bash都可以使用。
10. a=\`命令\`中的命令可以直接执行。等价于 a=$(pwd)

### 特殊变量

- $0 脚本名
- $1 第一个参数
- $2 第二个参数
- $# 输入参数个数
- $@ 参数列表
- $* 参数列表
- $? 上条命令的执行状态，0代表执行成功

## 运算符

介绍常用的运算符

### 基本运算式

1. $[运算式] 或者 $((运算式))
2. expr +,-,\*,// 注意运算符两侧要有空格。计算1+1=2. *expr \`expr 1 + 2 * 2\`*

### 条件判断

\[ condition   \]

1. 非空返回true
2. =用于字符串比较，整数之间的比较用-eq之类的命令 [ 1 -gt 0 ]
3. 权限操作\[ -r first.sh \]
4. 文件操作\[ -e first.sh \]

### 流程控制

#### If的使用

```bash
#!/bin/bash       告诉别人该程序用bash执行
#获取参数，并判断参数是什么
    if [ $1 -eq 1]
        then echo 输出数字是1
            elif [ $1 -eq 2] : then 输出数字是2
            elif [ $1 -eq 3] : then 输出数字是3
    fi #fi结尾
```

#### case

```bash
#!/bin/bash       告诉别人该程序用bash执行
    case $1 in
    "guest") echo "欢迎";;
    "admin") echo "欢迎管理员";;
    "*") echo "886";;
    esac
```

#### for

```bash
#!/bin/bash       告诉别人该程序用bash执行
#求1到100的和
sum=0
for((i=1;i<=100;i++))
do
    sum=$[$i+$sum]
done
echo "1到100的和是:$sum"
```

```bash
#!/bin/bash       告诉别人该程序用bash执行
#使用增强for遍历集合
for i in jack tom marry
do
    echo "$i是好孩子"
done

for j in @*
do
    echo "$j是好孩子"
done
```

#### while

```bash
#!/bin/bash       告诉别人该程序用bash执行
#求1到100的和
sum=0
i=0
while ((i<=100))
do
    sum=$[$sum+$i]
    let i++
done
echo "1到100的和是:$sum"
```

## 常用函数

read -p "请输入你的参数" -t 10 NUM , 提示输入

basename 基本语法 basename[string/pathname][suffix] 获取基本名,去掉路径

dirname 基本语法 dirname[string/pathname][suffix] 获取路径

## 自定义函数

```bash
#!/bin/bash
# 自定义函数基本语法,返回值如果是0则运行成功
function funname[()]
{

    Action;

    [return int;]

}
```

```bash
#!/bin/bash
# 一个求和函数,返回值如果是0则运行成功
function add[()]
{

    result=$[$1+$2]
    echo "$result"

}
read -p "请输入第一个参数" NUM1
read -p "请输入第二个参数" NUM2
add $NUM1 $NUM2
```

## SHELL 工具

### wc

用法：wc filename

参数有-w统计单词数，-l行数，-c字节数.

### cut

用法：cut filename

-f fileds, 提取第几列
-d descriptor 按照指定分隔符分割列

echo $PATH | cut $PATH  -d : -f 1,4-7

### sed

用法：sed [选项参数] 'command'   filename

sed '2a nihao' filename 在第二行插入你好
sed '/wo/ d' filename   删除wo
在sed中用','连接连续的数字，$表示最后一行，^表示第一行

sed 's/wo/ni/g' sed.txt
加上g全局替换。

需要同时进行多个动作时需要加入-e

### sort

用法：sort filename
sort filename
-n 按数值排序， -k 3 按第三列排 -u 去重 -r 逆序

## AWK（自有语法）

用法 [选项参数] 'pattern{action1} pattern2{action2}...' filename\
pattern：表示AWK在数据查找的内容，就是匹配模式 -F指定分隔符， -v赋值一个用户的自定义变量\
action: 在找到匹配内容时所执行的一系列命令\
print 表示输出
$1 $2 表示第一行第二行
awk -F : '/^root/ {print $1 "," $7}' /etc/passwd
-v定义一个用户变量
显示以root开头的行的第一列和第七列\
定义了关键字 BEGIN:代表一个初始化方法\
END代表一个结束方法\
FILENAME:文件名\
NR:行数\
NF:列数


## 

tail 监控文件
