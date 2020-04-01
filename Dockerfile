FROM php:7.4-apache
RUN a2enmod rewrite
RUN docker-php-ext-install mysqli
COPY src/ /var/www/html/
EXPOSE 80
