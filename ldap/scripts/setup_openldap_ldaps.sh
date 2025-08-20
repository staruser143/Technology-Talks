
#!/bin/bash

# Exit on error
set -e

# Install OpenLDAP and utilities
if [ -f /etc/debian_version ]; then
    sudo apt update
    sudo apt install -y slapd ldap-utils
elif [ -f /etc/redhat-release ]; then
    sudo yum install -y openldap openldap-servers openldap-clients
fi

# Set LDAP admin password
sudo dpkg-reconfigure slapd

# Create LDIF files for base structure
cat <<EOF > base.ldif
dn: dc=example,dc=com
objectClass: top
objectClass: dcObject
objectClass: organization
o: Example Corp
dc: example

dn: ou=users,dc=example,dc=com
objectClass: organizationalUnit
ou: users

dn: ou=groups,dc=example,dc=com
objectClass: organizationalUnit
ou: groups
EOF

# Add base structure
ldapadd -x -D "cn=admin,dc=example,dc=com" -W -f base.ldif

# Create LDIF for user
cat <<EOF > user.ldif
dn: uid=jdoe,ou=users,dc=example,dc=com
objectClass: inetOrgPerson
uid: jdoe
cn: John Doe
sn: Doe
userPassword: $(slappasswd -s password123)
EOF

# Add user
ldapadd -x -D "cn=admin,dc=example,dc=com" -W -f user.ldif

# Create LDIF for group
cat <<EOF > group.ldif
dn: cn=webapp-users,ou=groups,dc=example,dc=com
objectClass: groupOfNames
cn: webapp-users
member: uid=jdoe,ou=users,dc=example,dc=com
EOF

# Add group
ldapadd -x -D "cn=admin,dc=example,dc=com" -W -f group.ldif

# Generate self-signed SSL certificate
openssl req -new -x509 -days 365 -nodes     -out /etc/ssl/certs/ldap-server.crt     -keyout /etc/ssl/private/ldap-server.key     -subj "/C=US/ST=State/L=City/O=Example Corp/CN=ldap.example.com"

# Configure OpenLDAP to use SSL
cat <<EOF > tls.ldif
dn: cn=config
changetype: modify
add: olcTLSCertificateFile
olcTLSCertificateFile: /etc/ssl/certs/ldap-server.crt
-
add: olcTLSCertificateKeyFile
olcTLSCertificateKeyFile: /etc/ssl/private/ldap-server.key
EOF

ldapmodify -Y EXTERNAL -H ldapi:/// -f tls.ldif

# Restart LDAP service
sudo systemctl restart slapd

# Test LDAPS connection
ldapsearch -H ldaps://localhost -x -b "dc=example,dc=com" -D "cn=admin,dc=example,dc=com" -W
