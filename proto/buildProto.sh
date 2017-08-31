#!/usr/bin/env sh
protoc --java_out=../src/ PechkinNodeAddress.proto
protoc --java_out=../src/ PechkinObjects.proto
protoc --java_out=../src/ PechkinPrivateData.proto
protoc --java_out=../src/ PechkinConfig.proto
