#!/bin/bash
read -p "Mensaje para el commit?: "  mensaje
git add .
git commit -m "$mensaje"
git push