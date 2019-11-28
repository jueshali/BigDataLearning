# HQL查询

## 基本查询

```sql
-- 使用的表名为student,字段有id,nam,age,class;

-- 全表查询
select * from student;

-- 特定列查询
select id,name from student;

-- 列别名

select name as stuName from student;
select name stuName form student;

-- 常用函数
--- 求行数
select count(*) num from student;
--- 求最大值
select max(age) maxAge from student;
--- 求最小值
select min(age) minAge from student;
--- 求总和
select sum(age) sumAge from student;
--- 求平均年龄
select avg(age) avgAge from student;

-- 限制行数
select * from student limit 4;

-- Where 子句，使用WHERE子句，将不满足条件的行过滤掉。
select * from student where age >10;
--- 运算符 [部分] 

---- A [not] between B and C 如果A,B,C有一个为NULL则返回NULL，如果A在值B和C之间则为true否则为false;
select * from student where age between 20 and 30;

---- A<=>B 如果A,B都为null则为true,只有一个为null则为null，否则为true 或false

----Like 和 Rlike
select * from student where name like 'tom%';
select * from student where age like '_2%';

----And 和 or
select * from student where age>30 and id <30;
select * from student where age>30 or id <30;
select * from student where age not in [23,25];

-- 分组,分组通常和聚合函数一起使用，按照一个或者多个队列的结果进行分组，然后对每个组执行聚合操作。注意使用分组后
-- select之后只能有聚集函数或者分组的字段
select * from student group by class;

-- Having,与where针对表中的列发挥作用不同，having是针对查询结果中的列发挥作用，筛选数据，havin只用于group by分组统计语句。

```
