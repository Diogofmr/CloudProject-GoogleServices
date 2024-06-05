sudo gcloud functions deploy logging-app --project=cn2324-t1-g18 --region=europe-west2  --entry-point=loggingApp.LoggingApp --no-gen2 --runtime=java11 --trigger-topic image-requests --source=target/deployment --service-account=cn-v2324-firestore-18@cn2324-t1-g18.iam.gserviceaccount.com
zsh
