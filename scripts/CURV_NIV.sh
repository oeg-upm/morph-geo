#!/bin/bash
for i in $(seq $1 $2); do
    j=$((i+1))
    sed -e "4s/NIV[0-9]*/NIV$i/" -e "557s/[0-9]/$i/" -e "573s/[0-9]/$j/" BTN100_0202L_CURV_NIV8.ktr > BTN100_0202L_CURV_NIV$i.ktr
    pan -file="$(pwd)/BTN100_0202L_CURV_NIV$i.ktr"
done

