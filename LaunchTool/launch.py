#!/usr/bin/env python3
from kubernetes import client, config

import argparse
import copy
import os
import subprocess
import sys
import yaml

def init():
	config.load_kube_config()
	v1 = client.CoreV1Api()
	return v1

def find_pods(v1):
	all_pods = v1.list_pod_for_all_namespaces(watch=False)
	def pod_filter(p):
		return p.metadata.namespace == 'default' and \
				len(p.spec.containers) == 1 and \
				p.spec.containers[0].image.startswith('edge-image') or \
				p.spec.containers[0].image.startswith('backend-image')
	pods_we_own = filter(pod_filter, all_pods.items)
	return pods_we_own

def boot_pod(v1, pod_spec, name):
	pod_spec = copy.deepcopy(pod_spec)

	pod_spec['metadata']['name'] = name

	try:
		response = v1.create_namespaced_pod('default', pod_spec)
	except:
		print('Could not launch pod')
		raise

def shutdown_pod(v1, name, namespace):
	response = v1.delete_namespaced_pod(name, \
		namespace,\
		client.V1DeleteOptions(), \
		grace_period_seconds=0, \
		propagation_policy='Foreground')

def shutdown_service(v1, name, namespace):
	response = v1.delete_namespaced_service(name, \
		namespace,\
		client.V1DeleteOptions(), \
		grace_period_seconds=0, \
		propagation_policy='Foreground')

def shutdown_pods(v1, pods):
	for pod in pods:
		try:
			shutdown_pod(v1, pod.metadata.name, pod.metadata.namespace)
		except:
			print('Error in killing %s %s'%(pod, e), out=sys.stderr)

def get_service(v1, service):
	return v1.list_service_for_all_namespaces(watch=False, field_selector='metadata.name=%s'%service) 

def show(args):
	v1 = init()
	pods = find_pods(v1)
	for pod in pods:
		print('%s'%pod.metadata.name)

def clean(args):
	v1 = init()
	all_pods = find_pods(v1)
	shutdown_pods(v1, all_pods)

	all_services = get_service(v1, 'edge-service')
	for svc in all_services.items:
		shutdown_service(v1, svc.metadata.name, svc.metadata.namespace)
	all_services = get_service(v1, 'backend-service')
	for svc in all_services.items:
		shutdown_service(v1, svc.metadata.name, svc.metadata.namespace)

def boot(args):
	v1 = init()
	with open(os.path.join(sys.path[0], 'pod-template.yaml')) as f:
		specs = list(yaml.load_all(f))
		edge_pod_spec = specs[0]
		backend_pod_spec = specs[2]
		num_of_pods = args.num
		edge_servers = ['edge-server%d'%i for i in range(num_of_pods)]
		print('start to build edge servers.')
		for edge_server in edge_servers:
			boot_pod(v1, edge_pod_spec, edge_server)
		print('start to build backend server')
		boot_pod(v1, backend_pod_spec, 'backend-server')

def kill(args):
	v1 = init()
	all_pods = find_pods(v1)
	pod_name = 'edge-server%d'%args.which
	pod_to_kill = list(filter(lambda i: i.metadata.name == pod_name, all_pods))
	if len(pod_to_kill) != 1:
		sys.exit(1)
	shutdown_pod(v1, pod_to_kill[0].metadata.name, pod_to_kill[0].metadata.namespace)

def launch(args):
	v1 = init()
	all_pods = find_pods(v1)
	all_pods_names = list(map(lambda i : i.metadata.name, all_pods))
	pod_name = 'edge-server%d'%args.which
	if pod_name in all_pods_names:
		print('%s is already running'%pod_name, out=sys.stderr)
		sys.exit(1)

	with open(os.path.join(sys.path[0], 'pod-template.yaml')) as f:
		specs = list(yaml.load_all(f))
		pod_spec = specs[0]
		boot_pod(v1, pod_spec, pod_name)

def log(args):
	v1 = init()
	all_pods = list(find_pods(v1))

	if len(all_pods) != 0:
		for pod in all_pods:
			print('pod name -> %s:'%pod.metadata.name)
			try:
				response = v1.read_namespaced_pod_log(pod.metadata.name, pod.metadata.namespace, pretty=True)
				print(response)
			except:
				print('Error in reading pod log from server')
	else:
		print('No pods exist')

def create_service(args):
	v1 = init()
	with open(os.path.join(sys.path[0], 'pod-template.yaml')) as f:
		specs = list(yaml.load_all(f))
		edge_svc_spec = specs[1]
		backend_svc_spec = specs[3]
		try:
			response = v1.create_namespaced_service('default', edge_svc_spec)
			response = v1.create_namespaced_service('default', backend_svc_spec)
		except:
			print('Error in creating a service')

def main():
	parser = argparse.ArgumentParser(prog=sys.argv[0])
	subparsers = parser.add_subparsers(help="sub-command help", dest='command')
	subparsers.required = True
	
	list_parser = subparsers.add_parser("list")
	list_parser.set_defaults(func=show)

	clean_parser = subparsers.add_parser("clean")
	clean_parser.set_defaults(func=clean)

	boot_parser = subparsers.add_parser("boot")
	boot_parser.add_argument('num', type=int, help='How many edge server replicas?')
	boot_parser.set_defaults(func = boot)

	kill_parser = subparsers.add_parser("kill")
	kill_parser.add_argument('which', type=int, help='Which edge server should die')
	kill_parser.set_defaults(func=kill)
	
	launch_parser = subparsers.add_parser("launch")
	launch_parser.add_argument('which', type=int, help='Which edge should be launched')
	launch_parser.set_defaults(func=launch)

	log_parser = subparsers.add_parser("logall")
	log_parser.set_defaults(func=log)

	# TODO: create services
	svc_parser = subparsers.add_parser("createsvc")
	svc_parser.set_defaults(func=create_service)

	args = parser.parse_args()
	args.func(args)

if __name__ == "__main__":
	main()
	sys.exit(0)
