#!/bin/sh
socat TCP-LISTEN:8080,fork TCP:spring-api:8080 &
apache2-foreground