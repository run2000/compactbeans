#!/bin/sh
#JAVA_EXE=/usr/bin/java
JAVA_EXE=~/java/compact1/bin/java
#JAVA_EXE=~/java/compact2/bin/java
#JAVA_EXE=~/java/compact3/bin/java

TESTPATH=%TESTPATH%
FULLJRE=%FULLJRE%

echo "Using java $JAVA_EXE"
echo "..."

$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4067824
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4072197
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4076065
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4092905
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4092906
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4144543
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4168833
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4274639
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4343723
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4353056
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4425885
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.x4520754.Test4520754
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4619536
if [ $FULLJRE -gt 0 ]
then
    $JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4619792
fi
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4896879
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4918902
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4948761
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4985020
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test4994635
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test5063390
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test5102804
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test6194788
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test6277246
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test6311051
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test6422403
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test6528714
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test6630275
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test6707234
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test6723447
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test6868189
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test6963811
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.x6976577.Test6976577
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.x7122138.Test7122138
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test7148143
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test7172865
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test7186794
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test7189112
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test7192955
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test7193977
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test8027648
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test8027905
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test8034085
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test8034164
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test8039776
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.Test8040656
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.TestEquals
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.TestListeners
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.TestSerialization
$JAVA_EXE -cp $TESTPATH org.compactbeans.beans.test.DescriptorDataTest
