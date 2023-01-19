FROM php:8.2.0-apache
RUN echo "ServerName localhost" >> /etc/apache2/apache2.conf
RUN apt update && apt install -y socat
COPY frontend-start.sh /usr/local/bin/frontend-start
COPY dashboard-frontend/ /var/www/html
COPY favicon.ico /var/www/html
ENTRYPOINT ["frontend-start"]