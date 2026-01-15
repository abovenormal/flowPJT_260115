#!/bin/bash
echo "Stopping extension-app..."
sudo systemctl stop extension-app || true
echo "Application stopped."
