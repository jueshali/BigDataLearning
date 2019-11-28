# HQL中的常用函数

对于函数的说明从三个方面出发，1、usage.2、example.3appliction situation

## NVL

usage：`nvl(T value, T default_value)`  Returns default value if value is null else returns value.

example: `select name,nvl(sex,"男") from stu;`查询stu表，返回姓名和性别，如果性别不存在，则返回男。

application:用于处理空值字段。

## CASE WHEN 和 IF

类似于编程语言中的case和if。

- `if(boolean testCondition, T valueTrue, T valueFalseOrNull)`
- `case a when b then c when d then e else f end`

example: `select`

```sql
set hive.exec.mode.local.auto =TRUE

-- 求不同部门男女各多少人
-- way1

SELECT t1.dept_id, t1.man,t2.woman
FROM
(SELECT
dept_id,COUNT(*) man
FROM emp_sex
WHERE sex='男'
GROUP by dept_id) t1
JOIN
(SELECT
dept_id,COUNT(*) woman
FROM emp_sex
WHERE sex='女'
GROUP by dept_id) t2
on t1.dept_id = t2.dept_id

-- way2
SELECT
dept_id,sum(CASE WHEN sex='男' THEN 1 ELSE 0 END) man,
sum(CASE WHEN sex='男' THEN 0 ELSE 1 END) woman
FROM emp_sex
GROUP by dept_id

-- way3
SELECT
dept_id, sum(IF(sex='男' ,1 ,0)) man,
sum(IF(sex='男' ,0 ,1)) woman
FROM emp_sex
group by dept_id
```

## collect_set和collect_list

usage：collect_set(col)  Returns a set of objects with duplicate elements eliminated.
example: concat_ws(',',collect_set(name));将同一组的多个人名返回，为一个集合并用，相连的方式将集合中的人名连接为字符串

```sql
/*
孙悟空	白羊座	A
大海	     射手座	A
宋宋	     白羊座	B
猪八戒    白羊座	A
凤姐	     射手座	A
 */
--行转列
CREATE TABLE person_info(
name string,
constellation string,
blood_type string
)
row format delimited fields terminated by '\t';


-- collect_set(列)：将一列数据合成一个集合，collect_list与set不同的是可以重复
-- concat(str1,str2):将多个字符串拼接在一起	
-- concat_ws("|",collect_set(t1.name)):将一个集合中的元素按照|进行拼接~是一个聚集函数

SELECT
t1.base, concat_ws("|",COLLECT_SET(t1.name)) name
FROM
(SELECT
name,
concat(TRIM(constellation),",",blood_type) base
FROM
person_info) t1
GROUP by t1.base

select
concat(trim(constellation),blood_type),concat_ws("|",COLLECT_SET(name))
FROM person_info
GROUP by trim(constellation),blood_type
```

## explode

usage：Explodes an array to multiple rows. Returns a row-set with a single column (col), one row for each element from the array.如果是map则炸成两列
example: select explode(map('A',10,'B',20,'C',30));
ps:由于炸裂后一行数据变成了多行，因此在很多情况下需要与LATERAL VIEW配合使用

```sql
-- 一列一行转一列多行 explode:将array炸裂成一列，将map炸裂成N行两列,
-- array是一个UDTF函数，不能用在select之外，必须紧挨着select,.不能嵌套在表达式之中。
-- 在select中，UDFS函数可以和LATERAL VIEW合在一起使用，它可以将UDTF炸裂的数据仍然在逻辑上是一行，使得炸裂之后的字段能拼接普通字段

SELECT
movie,cname
FROM movie_info LATERAL VIEW explode(category) t as cname
```

## 开窗over()

usage: 通过开窗函数可以对查询的数据分组，排序，限制查询的范围
example:(ROWS | RANGE) BETWEEN (UNBOUNDED | [num]) PRECEDING AND ([num] PRECEDING | CURRENT ROW | (UNBOUNDED | [num]) FOLLOWING)

## 窗口函数

支持的在over()之前要窗口函数才有作用

1. 提供的专用窗口函数（配合order by 才有意义）

    - LEAD（colum_name,n,defalut_value）：返回后第n行的数据,如果为空时使用默认值
    - LAG（colum_name,n,defalut_value）：返回前第n行的数据，如果为空时使用默认值
    - FIRST_VALUE（colum_name,true|false）:返回第一行的数据，true和false指示是否跳过空值
    - LAST_VALUE（colum_name,true|false）:返回最后一行的数据，true和false指示是否跳过空值

2. 聚集函数:不多分析

    - COUNT(colum_name)
    - SUM(colum_name)
    - MIN(colum_name)
    - MAX(colum_name)
    - AVG(colum_name)

3. 排序函数（配合order by 才有意义）

    - RANK():按照分区排名，1，2，3，3，5
    - ROW_NUMBER（）1，2，3，4，5
    - DENSE_RANK（）1，2，3，3，4
    - CUME_DIST（）0.25，0.5，0.75，1 n/all
    - PERCENT_RANK（）(n-1)/(all-1)
    - NTILE（n）将数据均分为n份

```sql
-- 窗口函数

/*
 
name	orderdate	cost
jack	2017-01-01	10
tony	2017-01-02	15
jack	2017-02-03	23
tony	2017-01-04	29
jack	2017-01-05	46
jack	2017-04-06	42
tony	2017-01-07	50
jack	2017-01-08	55
mart	2017-04-08	62
mart	2017-04-09	68
neil	2017-05-10	12
mart	2017-04-11	75
neil	2017-06-12	80
mart	2017-04-13	94
 */


--1.查询在2017年4月份购买过的顾客及总人数

--- 错误,此处count统计的窗口为group by之后，mart有4人，Jack有一人，并非总人数
SELECT
name,count(name)
FROM business WHERE SUBSTRING(orderdate,1,7)='2017-04'
GROUP by name

--- 正确，通过over开窗，over统计的就是外面的字段
SELECT
name,COUNT(name) OVER()
FROM business WHERE SUBSTRING(orderdate,1,7)='2017-04'
GROUP by name

--2.查询顾客的购买明细及月购买总额

SELECT
*,sum(cost) OVER(PARTITION BY name, SUBSTRING(orderdate,1,7) ROWS BETWEEN UNBOUNDED PRECEDING and UNBOUNDED FOLLOWING)
FROM business

--3.上述的场景,要将cost按照日期进行累加
SELECT
*,SUM(cost) OVER(PARTITION BY name, SUBSTRING(orderdate,1,7) ORDER BY orderdate ROWS BETWEEN UNBOUNDED PRECEDING and CURRENT ROW)
FROM business

--4.查看顾客的明细和上次的购买时间
SELECT
*,lag(orderdate,1,'第一次购买') OVER(PARTITION by name order by orderdate ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)
FROM business

--5.查看顾客的明细和下一笔的购买时间
SELECT
*,lead(orderdate,1,'最后一次购买') OVER(PARTITION by name order by orderdate ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)
FROM business

--6.查询每个顾客每月明细和第一笔订单时间！
SELECT 
*,FIRST_value(orderdate) OVER(PARTITION by name,SUBSTRING(orderdate,1,7) order by orderdate ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)
FROM business

--7.查看顾客的消费明细，以及最近三笔消费总额
--①当前一笔+之前最近两笔
SELECT 
*,sum(cost) OVER(PARTITION by name order by orderdate ROWS BETWEEN 2 PRECEDING AND CURRENT ROW)
FROM business

--②当前一笔+之前最近的一笔+之后最近的一笔
SELECT 
*,sum(cost) OVER(PARTITION by name order by orderdate ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING)
FROM business

-- 查询前20%的订单
SELECT 
*
FROM
(SELECT 
*,NTILE(5) OVER(order by orderdate) nl
FROM business) tmp
where tmp.nl=1


/*
 name	subject	score
孙悟空	语文	87
孙悟空	数学	95
孙悟空	英语	68
大海	语文	94
大海	数学	56
大海	英语	84
宋宋	语文	64
宋宋	数学	86
宋宋	英语	84
婷婷	语文	65
婷婷	数学	85
婷婷	英语	78
 */

create table score(
name string,
subject string, 
score int) 
row format delimited fields terminated by "\t";

load data LOCAL inpath '/opt/module/apache-hive-1.2.1-bin/data/data11' into TABLE score


-- 按学科排名
SELECT
*,RANK() OVEr(PARTITION BY subject ORDER BY score desc ROWS BETWEEN UNBOUNDED PRECEDING and UNBOUNDED FOLLOWING)
FROM score

-- 按照总成绩牌名
SELECT
t1.name,t1.scoreSum, RANK() OVER(ORDER by t1.scoreSum)
FROM
(SELECT
name,SUM(score) scoreSum
FROM score
GROUP by name) t1


SELECT
name,sum(score),rank() OVER(ORDER by sum(score))
FROM score
GROUP by name

-- 只查询每个科目的成绩的前2名
SELECT *
FROM(
SELECT
*,RANK() OVER (PARTITION by subject ORDER by name DESC ) rk
FROM score
) t1
WHERE t1.rk <=2
-- 查询学生成绩，并显示当前科目最高分

SELECT
*,FIRST_value(score) OVER(PARTITION by subject ORDER by score desc)
FROM score

-- 查询学生成绩，并显示当前科目最低分

SELECT
*,LAST_value(score) OVER(PARTITION by subject ORDER by score desc rows BETWEEN UNBOUNDED PRECEDING and UNBOUNDED FOLLOWING)
FROM score


SELECT
*,PERCENT_RANK() OVER(PARTITION by subject order by score DESC)
FROM score
```
