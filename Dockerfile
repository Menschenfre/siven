FROM php:7.4-apache
a2enmod rewrite
COPY src/ /var/www/html/
EXPOSE 80
