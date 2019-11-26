# Hive的数据类型

由于hive是通过java程序编写的，所以hive的数据类型和java有很多共同之处

Hive数据类型-->Java数据类型\
TINYINT-->byte\
SMALINT--> short\
INT-->int\
BIGINT-->long\
BOOLEAN-->boolean\
FLOAT-->float\
DOUBLE-->double\
STRING-->string

同时在hive中也提供了集合数据类型
STRUCT：可以通过“点”符号访问元素内容。
MAP:MAP是一组键-值对元组集合，使用数组表示法可以访问数据。
ARRAY:数组是一组具有相同类型和名称的变量的集合。
STRUCT和MAP的区别在于MAP中的key是可变的。

与Java一样，Hive的原子类型是可以进行隐式转换的，低精度会隐式的转换为高精度。但不会反向转化，除非使用cast命令。

```sql
select cast(phonenum as int) from p1;
```
