#!/bin/bash
DEST=../build

if ! [ -d $DEST ] ; then
    mkdir $DEST
else
    rm $DEST/*.png
fi

for i in *.png; do
    n=$(echo $i | sed -e "s/.png//"); 
    echo $n; 
    convert $i -resize 24x24 $DEST/${n}24.png; 
    convert $i -resize 16x16 $DEST/${n}.png; 
    convert $i +level 60%,70% -resize 24x24 $DEST/${n}24_disabled.png; 
    convert $i +level 60%,70% -resize 16x16 $DEST/${n}_disabled.png; 
done
