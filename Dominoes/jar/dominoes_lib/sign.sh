#!/bin/bash

for filename in ./*.jar; do jarsigner -keystore ../dominoesKey -storepass delphi10 $filename jdc;done
