#!/bin/sh
echo "$accurevclientdockerimage" | git clone https://github.com/WiMills/accurev-client-docker.git
docker build -t accurev-client ./accurev-client-docker
