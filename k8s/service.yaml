# k8s/service.yaml
apiVersion: v1                               # Service: https://k8s.io/docs/concepts/services-networking/service/
kind: Service
metadata:
  name: my-service
  namespace: apps
spec:
  selector:                                   # Какие поды балансировать
    app: my-service
  ports:
    - name: http
      port: 80                                  # Порт сервиса
      targetPort: 8080                          # Порт контейнера
      protocol: TCP
  type: ClusterIP                             # Внутрикластерный доступ (внешний — через Ingress/LoadBalancer)
