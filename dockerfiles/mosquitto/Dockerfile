FROM eclipse-mosquitto:2.0.21-openssl

COPY ./mosquitto/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]