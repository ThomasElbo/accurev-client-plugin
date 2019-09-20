#!/bin/sh
git clone https://$accurevclientdockerimage@github.com/WiMills/accurev-client-docker.git
docker build -t accurev-client ./accurev-client-docker
