var Grafana = Vue.component("grafana", {
	template: `
		<div id="grafana">
			<header>
				<ul class="breadcrumbs">
					<li class="namespace">
						<svg xmlns="http://www.w3.org/2000/svg" width="20.026" height="27"><g fill="#00adb5"><path d="M1.513.9l-1.5 13a.972.972 0 001 1.1h18a.972.972 0 001-1.1l-1.5-13a1.063 1.063 0 00-1-.9h-15a1.063 1.063 0 00-1 .9zm.6 11.5l.9-8c0-.2.3-.4.5-.4h12.9a.458.458 0 01.5.4l.9 8a.56.56 0 01-.5.6h-14.7a.56.56 0 01-.5-.6zM1.113 17.9a1.063 1.063 0 011-.9h15.8a1.063 1.063 0 011 .9.972.972 0 01-1 1.1h-15.8a1.028 1.028 0 01-1-1.1zM3.113 23h13.8a.972.972 0 001-1.1 1.063 1.063 0 00-1-.9h-13.8a1.063 1.063 0 00-1 .9 1.028 1.028 0 001 1.1zM3.113 25.9a1.063 1.063 0 011-.9h11.8a1.063 1.063 0 011 .9.972.972 0 01-1 1.1h-11.8a1.028 1.028 0 01-1-1.1z"/></g></svg>
						<router-link :to="'/overview/'+currentNamespace" title="Namespace Overview">{{ currentNamespace }}</router-link>
					</li>
					<li>
						<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><path d="M10 0C4.9 0 .9 2.218.9 5.05v11.49C.9 19.272 6.621 20 10 20s9.1-.728 9.1-3.46V5.05C19.1 2.218 15.1 0 10 0zm7.1 11.907c0 1.444-2.917 3.052-7.1 3.052s-7.1-1.608-7.1-3.052v-.375a12.883 12.883 0 007.1 1.823 12.891 12.891 0 007.1-1.824zm0-3.6c0 1.443-2.917 3.052-7.1 3.052s-7.1-1.61-7.1-3.053v-.068A12.806 12.806 0 0010 10.1a12.794 12.794 0 007.1-1.862zM10 8.1c-4.185 0-7.1-1.607-7.1-3.05S5.815 2 10 2s7.1 1.608 7.1 3.051S14.185 8.1 10 8.1zm-7.1 8.44v-1.407a12.89 12.89 0 007.1 1.823 12.874 12.874 0 007.106-1.827l.006 1.345C16.956 16.894 14.531 18 10 18c-4.822 0-6.99-1.191-7.1-1.46z"/></svg>
						<router-link :to="'/overview/'+currentNamespace" title="Namespace Overview">SGClusters</router-link>
					</li>
					<li>
						<router-link :to="'/cluster/status/'+$route.params.namespace+'/'+$route.params.name" title="Status">{{ $route.params.name }}</router-link>
					</li>
					<li>
						Monitoring
					</li>
				</ul>

				<div class="actions">
				<a class="documentation" href="https://stackgres.io/doc/latest/04-postgres-cluster-management/01-postgres-clusters/" target="_blank" title="SGCluster Documentation">SGCluster Documentation</a>
					<div>
						<a  class="cloneCRD" @click="cloneCRD('SGCluster', currentNamespace, $route.params.name)">Clone Cluster</a>
						<router-link :to="'/crd/edit/cluster/'+$route.params.namespace+'/'+currentCluster.name">Edit Cluster</router-link>
						<a v-on:click="deleteCRD('sgcluster', currentNamespace, currentCluster.name, '/overview/'+currentNamespace)" :class="'/overview/'+currentNamespace">Delete Cluster</a>
					</div>
				</div>

				<ul class="tabs">
					<li>
						<router-link :to="'/cluster/status/'+$route.params.namespace+'/'+currentCluster.name" title="Status" class="status">Status</router-link>
					</li>
					<li>
						<router-link :to="'/cluster/configuration/'+$route.params.namespace+'/'+currentCluster.name" title="Configuration" class="info">Configuration</router-link>
					</li>
					<li v-if="currentCluster.hasBackups">
						<router-link :to="'/cluster/backups/'+$route.params.namespace+'/'+currentCluster.name" title="Backups" class="backups">Backups</router-link>
					</li>
					<li v-if="typeof currentCluster.data.spec.distributedLogs !== 'undefined'">
						<router-link :to="'/cluster/logs/'+$route.params.namespace+'/'+$route.params.name" title="Distributed Logs" class="logs">Logs</router-link>
					</li>
					<li v-if="currentCluster.data.grafanaEmbedded">
						<router-link id="grafana-btn" :to="'/monitor/'+$route.params.namespace+'/'+currentCluster.name" title="Grafana Dashboard" class="grafana">Monitoring</router-link>
					</li>
				</ul>

				<ul class="selector">
					<li><strong>Select a node:</strong></li>
					<li v-for="pod in pods">
						<router-link :to="'/monitor/'+currentCluster.name+'/'+pod.ip+':9187'">{{ pod.name }}</router-link>
					</li>
				</ul>
			</header>

			<div class="content grafana">
				<iframe :src="grafana" id="grafana"></iframe>
			</div>
		</div>`,
	data: function() {

		return {
			grafana: ""
		}
	},
	created: function() {
		this.fetchData()
	},
	computed: {

		pods () {
			return store.state.currentCluster.data.pods
		},

		currentNamespace () {
			return store.state.currentNamespace
		},

		currentCluster () {
			return store.state.currentCluster
		},

		theme () {
			return store.state.theme
		}

	},
	methods: {
		fetchData () {
			// Grafana service
			let vc = this;

			$.get("/grafana", function(data) {
				let url = data;
				url += (url.includes('?') ? '&' : '?') + 'theme='+vc.theme+'&kiosk';

				if(vc.$route.params.pod && vc.$route.params.pod.length)
					url = url+'&var-instance='+vc.$route.params.pod;
				else
					url = url+'&var-instance='+vc.pods[0].ip+':9187';
				vc.grafana = url;
				$(".grafana iframe").prop("src", url);
			});
		}
	}
})