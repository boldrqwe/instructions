apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-service
  namespace: apps
  labels:
    app: my-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: my-service
  template:
    metadata:
      labels:
        app: my-service
    spec:
      # ① если приватный GHCR — пусть поды сами знают секрет
      imagePullSecrets:
        - name: ghcr-creds

      # ② initContainer подготовит папку /app/logs
      initContainers:
        - name: prepare-logs
          image: busybox:1.36
          command: ["sh","-c","mkdir -p /app/logs && chmod 0777 /app/logs"]
          volumeMounts:
            - name: logs
              mountPath: /app/logs

      containers:
        - name: my-service
          image: ${IMAGE}
          ports:
            - containerPort: 8080
          # твои requests/limits/пробы оставь как были
          # livenessProbe/readinessProbe уже есть в твоём манифесте
          envFrom:
            - configMapRef:
                name: my-service-config
                optional: false
            - secretRef:
                name: my-service-secrets
                optional: false
          volumeMounts:
            - name: logs
              mountPath: /app/logs

      # ③ сам том для логов (эпhemeral)
      volumes:
        - name: logs
          emptyDir: {}
