# Combiner

Combiner的本质就是一个Reducer,作用就是合并,和Reducer的区别在于两者运行的阶段不同,Combiner运行再shuffle阶段,Reducer在Reducer阶段运行.它的设计就是为了优化shuffle流程,节省每次溢写 的磁盘IO和网络IO.\

Combiner将可能发生三次

- 在Maptask之中,Combiner在每次溢写之前,对缓冲区中的数据进行合并,并将合并后的数据溢写到磁盘.减少到磁盘的Io.
- 在最后的mergeParts阶段,如果设置了Combiner,且溢写的片段大于三,则在执行一次combiner
- Shuffle进程在拷贝多个同一分区的数据到ReduceTask后需要执行合并和排序,如果拷贝的数据超过了shuffle的内存,将部分数据排序后,如果设置了Combiner,在溢写时也会执行Combiner.

```java
if (combinerRunner == null) {
              // spill directly
              DataInputBuffer key = new DataInputBuffer();
              while (spindex < mend &&
                  kvmeta.get(offsetFor(spindex % maxRec) + PARTITION) == i) {
                final int kvoff = offsetFor(spindex % maxRec);
                int keystart = kvmeta.get(kvoff + KEYSTART);
                int valstart = kvmeta.get(kvoff + VALSTART);
                key.reset(kvbuffer, keystart, valstart - keystart);
                getVBytesForOffset(kvoff, value);
                writer.append(key, value);
                ++spindex;
              }
            } else {
              int spstart = spindex;
              while (spindex < mend &&
                  kvmeta.get(offsetFor(spindex % maxRec)
                            + PARTITION) == i) {
                ++spindex;
              }
              // Note: we would like to avoid the combiner if we've fewer
              // than some threshold of records for a partition
              if (spstart != spindex) {
                combineCollector.setWriter(writer);
                RawKeyValueIterator kvIter =
                  new MRResultIterator(spstart, spindex);
                combinerRunner.combine(kvIter, combineCollector);
              }
            }
```

```java
//如果分区数不足三就不会发生第二次溢写
 if (combinerRunner == null || numSpills < minSpillsForCombine) {
            Merger.writeFile(kvIter, writer, reporter, job);
          } else {
            combineCollector.setWriter(writer);
            combinerRunner.combine(kvIter, combineCollector);
        }
```
