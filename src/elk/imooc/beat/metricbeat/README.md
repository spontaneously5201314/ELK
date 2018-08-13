sudo chown root metricbeat.yml
sudo chown root modules.d/system.yml
sudo ./metricbeat -e -c metricbeat.yml -d "publish"

./metricbeat setup --dashboards