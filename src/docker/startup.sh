#!/bin/bash

# Overwrite cnf files, setting hostName to containers ID
sed -i "2s/.*/MASTER_SERVER = ${HOSTNAME}/" ./accurev/bin/acserver.cnf
# Set client.cnf to just target localhost defaultport 5050
sed -i "1s/.*/SERVERS = localhost:5050/" ./accurev/bin/acclient.cnf

# Start the database, we need to update its content, because we have a new hostName
./accurev/bin/acserverctl dbstart
# Update accurev information, login as default user postgres with password 1234
printf '1234\ny\n' | ./accurev/bin/maintain server_properties update postgres

# Start the remaining Accurev services, Accurev included
./accurev/bin/acserverctl start
# Create a first user in Accurev
./accurev/bin/accurev mkuser accurev_user
# Login as the Accurev_user - on first login it has no password
printf '\n' | ./accurev/bin/accurev login accurev_user
# Set the password for accurev_user to docker
./accurev/bin/accurev chpasswd accurev_user docker
echo "Accurev server is now ready. Enjoy!"
# Keep the container running after setup
sleep infinity;
