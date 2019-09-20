#!/bin/sh
git clone https://$accurevclientdockerimage/WiMills/accurev-client-docker.git
docker build -t accurev-client ./accurev-client-docker
