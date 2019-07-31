FROM ubuntu

# Create a user, Accurev can not run on root
RUN adduser --disabled-password --gecos '' accurev-user

# Run as accurev-user
USER accurev-user

# Set working directory to accurev-users home
WORKDIR /home/accurev-user

RUN mkdir ./accurev

# Copy the Accurev license file
COPY aclicense.txt ./aclicense.txt
# Copy the Accurev binary file
COPY accurev-7.3-linux-x86-x64.bin ./accurev-7.3-linux-x86-x64.bin
# Copy startup script
COPY startup.sh ./startup.sh

# Run the Accurev anywhere installation with defaults
RUN printf '\nY\n\n\n\n\n\n\n\n1234\n1234\n\n\n\n\n2\n\n\n\n\n' | ./accurev-7.3-linux-x86-x64.bin

# Run the startup script on container start
CMD ["./startup.sh"]
# For debug purposes
#CMD ["tail", "-f", "/dev/null"]
