#!/bin/bash

function test[()]
{

if (($#!=1));then
echo one para!;
exit;
fi

for((i=101;i<=103;i++))
do
    pathname='cd -P $(dirname $1); pwd'
    filename=basename $1
    rsync  -rvlt  $pathname/$basename   hadoop$i:$pathname
done
}