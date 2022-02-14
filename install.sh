
#!/bin/bash
#脚本用于更新阿里yum源、安装docker、安装Python3、安装docker-compose、配置pip国内镜像
#日期：2020-8-12

#更新yum为阿里yum源
yum_install(){
  mv /etc/yum.repos.d/CentOS-Base.repo /etc/yum.repos.d/CentOS-Base.repo.bak
  echo '备份成功，开始下载yum源'
  curl -o /etc/yum.repos.d/CentOS-Base.repo http://mirrors.aliyun.com/repo/Centos-7.repo
  echo '下载成功'
  yum clean all
  yum makecache
}
#配置pip国内镜像
pips(){
	mkdir -p /~.pip
	tee ~.pip/pip.conf <<-'EOF'
	[global]
	index-url=http://mirrors.aliyun.com/pypi/simple
	trusted-host=mirrors.aliyun.com
	EOF
}
#配置docker阿里镜像加速
docker_daemon(){
	mkdir -p /etc/docker
	tee /etc/docker/daemon.json <<-'EOF'
	{
		"registry-mirrors": ["https://m0gqm8ls.mirror.aliyuncs.com"]
	}
	EOF
	systemctl daemon-reload
	systemctl restart docker
}
#检测环境是否安装docker
dockers(){
  total=$(find / -name "docker.service")
  if [[ -z $total ]]; then
    echo '检测到系统未安装docker'
    return 0
  else
    echo '检测到系统已安装docker'
    return 1
fi
}
#检测环境是否安装docker-compose
docker_composes(){
  service=$(docker-compose -v |grep version)
  if [[ -n $service ]]; then
    echo '检测到系统已安装docker-compose，将自动启动服务'
    docker_compose_run
    echo '启动完成！！'
    echo '该脚本所在目录存在docker-compose.yml文件，手动启动请在该文件所在目录下执行docker-compose up -d 后台运行docker-compose项目启动所有服务'
    exit
  else
    echo '未安装docker-compose将进行安装'
    return 1
  fi
}
#启动docker服务并设置为自启动
docker_run(){
  systemctl start docker
  systemctl enable docker.service
}
#判断docker服务是否存在
docker_service(){
  for ((i=0;i<=3;i++))
  do
    service=$(systemctl status docker|grep Active|grep running)
    if [[ -z $service ]];then
      echo 'docker服务已启动'
      return 0
    else
      echo 'docker服务没有启动,尝试启动'
      docker_run
      sleep 3
      echo '再次检测docker服务是否启动'
    fi
  done
}
#安装docker
docker_install(){
  yum -y install docker
  echo '启动docker'
  docker_run
  docker_service
  docker_daemon

}
#安装Python3和docker-compose
docker_compose_install(){
  yum -y install python3
  pips
  pip3 install docker-compose
  echo '安装完成'
}
docker_compose_run(){
  docker_daemon
  docker-compose up -d
  exit
}
#主流程
echo '********安装docker-compose部署influxdb、telegraf、grafana、jmeter服务*********'
read -r -p "是否需要将yum源更换为阿里源[y/n]: " input
case $input in
  [yY][eE][sS]|[yY])
    echo '开始更新yum源'
    yum_install
    echo '检测系统是否安装docker'
    dockers
    if [[ $? -eq 0 ]]; then
      echo '开始docker安装'
      docker_install
      echo '开始Python3和docker-compose安装'
      docker_compose_install
      echo '启动docker-compose'
      docker-compose_run
    else
      echo '检测系统是否安装docker-compose'
      docker_composes
      docker_compose_install
      docker-compose_run
      exit
    fi
    ;;
  [nN][oO]|[nN])
    echo '检测是否安装docker'
    dockers
    if [[ $? -eq 0 ]]; then
      echo '开始docker安装'
      docker_install
      echo '开始Python3和docker-compose安装'
      docker_compose_install
      echo '启动docker-compose'
      docker-compose_run
    else
      docker_composes
      if [[ $? -eq 1 ]]; then
        docker_compose_install
        echo '正在启动docker-compose'
        docker_compose_run
      fi
    fi
  ;;
esac

