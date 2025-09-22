apiVersion: v1
kind: Service
metadata:
  name: my-service
  namespace: apps
spec:
  selector:
    app: my-service
  ports:
    - name: http
      port: 80
      targetPort: 8080
      nodePort: 30080
      protocol: TCP
  type: NodePort
