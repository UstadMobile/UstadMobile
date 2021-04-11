# Installing Ustad Mobile on your own server

The Ustad Mobile server runs as a standalone Java JAR file (powered by KTOR and Netty) It. can be
connected to an SQLite or PostgreSQL database. It is recommended to use Apache or Nginx as a proxy.
The server should run fine on any JVM platform, but has been tested on most extensively on Linux
(Ubuntu 20.04).

## Create a user, directory, and copy files

Create a user (optional) and a directory where the server and its data will be stored.
```
$ adduser ustad
$ sudo -u ustad -s
$ mkdir /home/ustad/server

# Copy the server JAR
$ cp /my/Download/ustad-server-all.jar /home/ustad/ustad-server-all.jar

# Install java (on Ubuntu)
$ apt-get install openjdk-8-jre
```

## Install HTTPS Certificate

It is strongly recommended to use https and HTTP2. Install a certificate for your server using
[EFF Certbot](https://certbot.eff.org/). Request a certificate as follows (turn off any server
running on port 80 when requesting the certificate).

```
export DOMAIN=mydomain.com
export EMAIL=me@mydomain.com
export ALIAS=myalias
certbot certonly -n -d $DOMAIN --email "$EMAIL" --agree-tos --standalone --preferred-challenges http
```

Convert the letsencrypt (or other) certificate to a jks (as per [KTOR https setup instructions](https://ktor.io/docs/ssl.html#ktor))
```
openssl pkcs12 -export -out /etc/letsencrypt/live/$DOMAIN/keystore.p12 -inkey /etc/letsencrypt/live/$DOMAIN/privkey.pem -in /etc/letsencrypt/live/$DOMAIN/fullchain.pem -name $ALIAS
# Provide a password

keytool -importkeystore -alias $ALIAS -destkeystore /etc/letsencrypt/live/$DOMAIN/keystore.jks -srcstoretype PKCS12 -srckeystore /etc/letsencrypt/live/$DOMAIN/keystore.p12
```

## Configure the server:

Unzip the example config file and edit it as required:
```
unzip ustad-server-all.jar application.conf
```

Edit the server config file as required:
```
ktor {
    security {
        ssl {
            keyStore = /etc/letsencrypt/live/mydomain.com/keystore.jks
            keyAlias = myalias
            keyStorePassword = password
            privateKeyPassword = password
        }
    }
}
```

## Run the server

Run the server using the Java command:

```
java -jar ustad-server-all.jar -config=application.conf
```

## Configure Apache HTTP Proxy (optional)

Enable the required modules:
```
sudo a2enmod proxy_http2 http2
```


Add the following to your Apache virtual host:
```
Protocols h2 http/1.1
ProxyPass /ustad/ h2://your.server.name:8889/
ProxyPassReverse /ustad/ https://your.server.name:8889/
SSLProxyEngine On

TimeOut 600
ProxyTimeout 600
RequestReadTimeout body=10,MinRate=1000
```

## Connect using the Ustad app

Open the Ustad app, and enter the address of your site. The admin password will be saved to
data/singleton/admin.txt
