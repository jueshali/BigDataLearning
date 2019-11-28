# SQL练习

# 蚂蚁森林植物申领统计
--问题：假设2017年1月1日开始记录低碳数据（user_low_carbon），假设2017年10月1日之前满足申领条件的用户都申领了一颗p004-胡杨，
--剩余的能量全部用来领取“p002-沙柳”?。
--统计在10月1日累计申领“p002-沙柳”?排名前10的用户信息；以及他比后一名多领了几颗沙柳。


- 日期需要变化吗？
- 将流水变化为存量
- 如果存量超过一个杨树的话，存量需要减少一颗杨树的量
- 计算领取的沙柳数并排序
- 计算他比后一名多领了几颗()

```sql
SELECT user_id,data_dt,low_carbon,carbon,rk,new_del
from
(SELECT
*, max(del) OVER(PARTITION by user_id) new_del
FROM
(SELECT
*,tree_num-lag(tree_num,1,0) OVER() del
FROM
(SELECT
*,DENSE_RANK() OVER(ORDER by tmp2.tree_num DESC) rk
FROM
(SELECT
*,FLOOR((tmp.carbon-215)/19) tree_num
FROM
(SELECT
*,sum(low_carbon) OVER (PARTITION by user_id ORDER BY data_dt ROWS BETWEEN UNBOUNDED PRECEDING and UNBOUNDED FOLLOWING) carbon
FROM 
user_low_carbon
) tmp) tmp2)tmp3 WHERE tmp3.rk <=10)tmp4 ORDER by rk)tmp5
```

##  蚂蚁森林低碳用户排名分析
问题：查询user_low_carbon表中每日流水记录，条件为：
用户在2017年，连续三天（或以上）的天数里，
每天减少碳排放（low_carbon）都超过100g的用户低碳流水。
需要查询返回满足以上条件的user_low_carbon表中的记录流水。
例如用户u_002符合条件的记录如下，因为2017/1/2~2017/1/5连续四天的碳排放量之和都大于等于100g：

```sql
SELECT
user_id,dt,low_car
FROM
(SELECT*, 
lag(sum_carbon,1,0) OVER(PARTITION by user_id ORDER by dt) pre1,
lag(sum_carbon,2,0) OVER(PARTITION by user_id ORDER by dt) pre2,
lead(sum_carbon,1,0) OVER(PARTITION by user_id ORDER by dt) next1,
lead(sum_carbon,2,0) OVER(PARTITION by user_id ORDER by dt) next2,
datediff(lag(dt,1,dt) OVER(PARTITION by user_id ORDER by dt) ,dt) pre1day,
datediff(lag(dt,2,dt) OVER(PARTITION by user_id ORDER by dt) ,dt) pre2day,
datediff(lead(dt,1,dt) OVER(PARTITION by user_id ORDER by dt) ,dt) next1day,
datediff(lead(dt,2,dt) OVER(PARTITION by user_id ORDER by dt) ,dt) next2day
FROM
(SELECT
user_id,regexp_replace(data_dt,'/','-') dt,sum(low_carbon) sum_carbon,collect_set(low_carbon) collec
FROM
user_low_carbon GROUP BY user_id,data_dt) tmp) tmp2 LATERAL VIEW explode(collec) t as low_car
WHERE (pre1>=100 AND pre2>=100 AND sum_carbon>=100 AND pre1day=-1 AND pre2day=-2) 
or (pre1>=100 AND next1>=100 AND sum_carbon>=100 AND pre1day=-1 AND next1day=1) 
or (next1>=100 AND next2>=100 AND sum_carbon>=100 AND next1day=1 AND next2day=2) 
```
