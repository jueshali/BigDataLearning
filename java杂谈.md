sleep是一个静态方法，当前运行线程（main）休眠，静态方法拿不到对象锁，不能释放对象锁
wait是一个成员方法，跟对象相关，是t2等待,能释放对象锁


>sleep()使当前线程进入停滞状态（阻塞当前线程），让出CUP的使用、目的是不让当前线程独自霸占该进程所获的CPU资源，以留一定时间给其他线程执行的机会;sleep()是Thread类的Static(静态)的方法；因此他不能改变对象的机锁，所以当在一个Synchronized块中调用Sleep()方法是，线程虽然休眠了，但是对象的机锁并木有被释放，其他线程无法访问这个对象（即使睡着也持有对象锁）。在sleep()休眠时间期满后，该线程不一定会立即执行，这是因为其它线程可能正在运行而且没有被调度为放弃执行，除非此线程具有更高的优先级。 

>wait()方法是Object类里的方法；当一个线程执行到wait()方法时，它就进入到一个和该对象相关的等待池中，同时失去（释放）了对象的机锁（暂时失去机锁，wait(long timeout)超时时间到后还需要返还对象锁）；其他线程可以访问；wait()使用notify或者notifyAlll或者指定睡眠时间来唤醒当前等待池中的线程。wiat()必须放在synchronized block中，否则会在program runtime时扔出”java.lang.IllegalMonitorStateException“异常。

java不是一个完全面向对象的语言，基本数字类型，有静态对象

java final: 
final 变量：修饰的是一个不可变变量
final 方法：不可重写
final 参数：
final 类：不可继承


什么叫加载一个类
public class User{
public static int age1 =20;
// final可以改变赋值操作的执行顺序
public static final int age2 =20;


{

  system.out.println(age1);
  system.out.println(age2);
  
}
}

怎么理解字符串的不可变：value[]数组是Final,地址不可以变，地址指向的内容可以变，但是字符串没有提供方法改变，可以通过反射

maven版本号
```java

//1.1.1.1
// 第一个1是核心变化
// 第二个1是功能补充
// 第三个数是Bug修复
// 第四个数是草稿
```

trim()只能去掉半角空格，不能去掉全角空格



public final class Hello
{
  public static void main(String[] paramArrayOfString)
  {
    Hello..MODULE$.main(paramArrayOfString);
  }
}


```java
public class TestDataType {
    public static void main(String[] args) {

        byte b  = 127;
        b = ++b;

        // = 是把等号右边的计算结果给左边，
        // i++的计算结果就是1
        int i = 0;
        int j = i ++;

        System.out.println(b);

    }
}
```


```java
integer i  = 100;
integer j = 100;

// 底层调用的是valueof
100.valueof()
```


java中的switch穿透

// 违背了开闭原则，允许对功能扩展，不允许修改
为什么List list = new LinkedList(t); 而不是ArrayList list = new ArrayList()

// 面向接口编程


java中的静态导入
import static 包名.类名.*
将静态方法导入‘


final修饰的吧变量只要在类初始化时初始化就行。不一定需要定义时就初始化

栈上分配: 把对象分配到栈之上

逃逸分析:当前的引用能不能从栈中逃出。

单例模式的单例使用后不能回收，可达性分析

接口多继承，

父类中的属性和方法一般使用protected方法声明访问权限


如果有抽象方法的话一定有抽象类


java中为什么没有多继承===？如果继承两个父类，两个父类都继承同一个父类，那么在子类中可能出现冲突

