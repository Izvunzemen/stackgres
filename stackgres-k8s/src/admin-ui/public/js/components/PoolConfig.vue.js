var PoolConfig = Vue.component("PoolConfig", {
	template: `
		<div id="pool-config">
			<header>
				<ul class="breadcrumbs">
					<li class="namespace">
						<svg xmlns="http://www.w3.org/2000/svg" width="20.026" height="27"><g fill="#00adb5"><path d="M1.513.9l-1.5 13a.972.972 0 001 1.1h18a.972.972 0 001-1.1l-1.5-13a1.063 1.063 0 00-1-.9h-15a1.063 1.063 0 00-1 .9zm.6 11.5l.9-8c0-.2.3-.4.5-.4h12.9a.458.458 0 01.5.4l.9 8a.56.56 0 01-.5.6h-14.7a.56.56 0 01-.5-.6zM1.113 17.9a1.063 1.063 0 011-.9h15.8a1.063 1.063 0 011 .9.972.972 0 01-1 1.1h-15.8a1.028 1.028 0 01-1-1.1zM3.113 23h13.8a.972.972 0 001-1.1 1.063 1.063 0 00-1-.9h-13.8a1.063 1.063 0 00-1 .9 1.028 1.028 0 001 1.1zM3.113 25.9a1.063 1.063 0 011-.9h11.8a1.063 1.063 0 011 .9.972.972 0 01-1 1.1h-11.8a1.028 1.028 0 01-1-1.1z"/></g></svg>
						<router-link :to="'/admin/overview/'+currentNamespace" title="Namespace Overview">{{ currentNamespace }}</router-link>
					</li>
					<li>
						<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 26.5 20"><path d="M14.305 18.749a4.7 4.7 0 01-2.388-.589 3.91 3.91 0 01-1.571-1.685 5.668 5.668 0 01-.546-2.568 5.639 5.639 0 01.548-2.561 3.916 3.916 0 011.571-1.678 4.715 4.715 0 012.388-.593 5.189 5.189 0 011.658.261 4.324 4.324 0 011.378.756.758.758 0 01.24.281.859.859 0 01.067.361.768.768 0 01-.16.495.479.479 0 01-.388.2.984.984 0 01-.548-.191 4 4 0 00-1.07-.595 3.405 3.405 0 00-1.1-.167 2.571 2.571 0 00-2.106.869 3.943 3.943 0 00-.72 2.562 3.963 3.963 0 00.716 2.568 2.568 2.568 0 002.106.869 3.147 3.147 0 001.063-.173 5.112 5.112 0 001.1-.589 2.018 2.018 0 01.267-.134.751.751 0 01.29-.048.477.477 0 01.388.2.767.767 0 01.16.494.863.863 0 01-.067.355.739.739 0 01-.24.286 4.308 4.308 0 01-1.378.757 5.161 5.161 0 01-1.658.257zm5.71-.04a.841.841 0 01-.622-.234.856.856 0 01-.234-.636v-7.824a.8.8 0 01.22-.6.835.835 0 01.609-.214h3.29a3.4 3.4 0 012.354.755 2.7 2.7 0 01.842 2.12 2.725 2.725 0 01-.842 2.127 3.386 3.386 0 01-2.354.764h-2.393v2.875a.8.8 0 01-.87.868zm3.05-5.069q1.779 0 1.779-1.552t-1.779-1.551h-2.18v3.1zM.955 4.762h10.5a.953.953 0 100-1.9H.955a.953.953 0 100 1.9zM14.8 7.619a.954.954 0 00.955-.952V4.762h4.3a.953.953 0 100-1.9h-4.3V.952a.955.955 0 00-1.909 0v5.715a.953.953 0 00.954.952zM.955 10.952h4.3v1.9a.955.955 0 001.909 0V7.143a.955.955 0 00-1.909 0v1.9h-4.3a.953.953 0 100 1.9zm6.681 4.286H.955a.953.953 0 100 1.905h6.681a.953.953 0 100-1.905z"></path></svg>
						SGPoolingConfigList
					</li>
					<li v-if="typeof $route.params.name !== 'undefined'">
						{{ $route.params.name }} 
					</li>
				</ul>

				<div class="actions">
					<a class="documentation" href="https://stackgres.io/doc/latest/04-postgres-cluster-management/02-configuration-tuning/03-connection-pooling-configuration/" target="_blank" title="SGPoolingConfig Documentation">SGPoolingConfig Documentation</a>
					<div>
						<router-link v-if="iCan('create','sgpoolconfigs',$route.params.namespace)" :to="'/admin/crd/create/connectionpooling/'+$route.params.namespace" class="add">Add New</router-link>
					</div>		
				</div>	
			</header>

			<div class="content">
				<table id="connectionpooling" class="configurations poolConfig">
					<thead class="sort">
						<th @click="sort('data.metadata.name')" class="sorted desc name">
							<span>Name</span>
						</th>
						<th class="config">
							Parameters
						</th>
						<th class="actions"></th>
					</thead>
					<tbody>
						<tr class="no-results">
							<td :colspan="3" v-if="iCan('create','sgpoolconfigs',$route.params.namespace)">
								No configurations have been found, would you like to <router-link :to="'/admin/crd/create/connectionpooling/'+$route.params.namespace" title="Add New Connection Pooling Configuration">create a new one?</router-link>
							</td>
							<td v-else colspan="3">
								No configurations have been found. You don't have enough permissions to create a new one
							</td>
						</tr>
						<template v-for="conf in config" v-if="(conf.data.metadata.namespace == currentNamespace)">
							<tr class="base" :class="[ $route.params.name == conf.name ? 'open' : '', 'sgpoolconfig-'+conf.data.metadata.namespace+'-'+conf.name ]" :data-name="conf.name">
								<td class="hasTooltip">
									<span>{{ conf.name }}</span>
								</td>
								<td class="parameters">
									<ul class="yaml" v-html="parseParams(conf.data.spec.pgBouncer['pgbouncer.ini'])"></ul>
								</td>
								<td class="actions">
									<router-link v-if="iCan('patch','sgpoolconfigs',$route.params.namespace) && !conf.data.status.clusters.length" :to="'/admin/crd/edit/connectionpooling/'+currentNamespace+'/'+conf.name" title="Edit Configuration">
										<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 14 14"><path d="M90,135.721v2.246a.345.345,0,0,0,.345.345h2.246a.691.691,0,0,0,.489-.2l8.042-8.041a.346.346,0,0,0,0-.489l-2.39-2.389a.345.345,0,0,0-.489,0L90.2,135.232A.691.691,0,0,0,90,135.721Zm13.772-8.265a.774.774,0,0,0,0-1.095h0l-1.82-1.82a.774.774,0,0,0-1.095,0h0l-1.175,1.176a.349.349,0,0,0,0,.495l2.421,2.421a.351.351,0,0,0,.5,0Z" transform="translate(-90 -124.313)"/></svg>
									</router-link>
									<a v-if="iCan('create','sgpoolconfigs',$route.params.namespace)" v-on:click="cloneCRD('SGPoolingConfig', currentNamespace, conf.name)" class="cloneCRD" title="Clone Configuration"><svg xmlns="http://www.w3.org/2000/svg" width="13.9" height="16" viewBox="0 0 20 20"><g><path fill="#00ADB5" d="M2.5,20c-0.5,0-1-0.4-1-1V5c0-0.5,0.4-1,1-1c0.6,0,1,0.4,1,1v12.4c0,0.3,0.3,0.6,0.6,0.6h9.4c0.5,0,1,0.4,1,1c0,0.5-0.4,1-1,1H2.5z"/><path fill="#00ADB5" d="M6.5,16c-0.5,0-0.9-0.4-0.9-0.9V0.9C5.6,0.4,6,0,6.5,0h11.1c0.5,0,0.9,0.4,0.9,0.9v14.1c0,0.5-0.4,0.9-0.9,0.9H6.5z M8,1.8c-0.3,0-0.6,0.3-0.6,0.6v11.2c0,0.3,0.3,0.6,0.6,0.6h8.1c0.3,0,0.6-0.3,0.6-0.6V2.4c0-0.3-0.3-0.6-0.6-0.6H8z"/><path fill="#00ADB5" d="M14.1,5.3H10c-0.5,0-0.9-0.4-0.9-0.9v0c0-0.5,0.4-0.9,0.9-0.9h4.1c0.5,0,0.9,0.4,0.9,0.9v0C15,4.9,14.6,5.3,14.1,5.3z"/><path fill="#00ADB5" d="M14.1,8.8H10C9.5,8.8,9.1,8.4,9.1,8v0c0-0.5,0.4-0.9,0.9-0.9h4.1C14.6,7.1,15,7.5,15,8v0C15,8.4,14.6,8.8,14.1,8.8z"/><path fill="#00ADB5" d="M14.1,12.4H10c-0.5,0-0.9-0.4-0.9-0.9v0c0-0.5,0.4-0.9,0.9-0.9h4.1c0.5,0,0.9,0.4,0.9,0.9v0C15,12,14.6,12.4,14.1,12.4z"/></g></svg></a>
									<a v-if="iCan('delete','sgpoolconfigs',$route.params.namespace) && !conf.data.status.clusters.length" v-on:click="deleteCRD('sgpoolconfig',currentNamespace, conf.name)" class="delete" title="Delete Configuration">
										<svg xmlns="http://www.w3.org/2000/svg" width="13.5" height="15" viewBox="0 0 13.5 15"><g transform="translate(-61 -90)"><path d="M73.765,92.7H71.513a.371.371,0,0,1-.355-.362v-.247A2.086,2.086,0,0,0,69.086,90H66.413a2.086,2.086,0,0,0-2.072,2.094V92.4a.367.367,0,0,1-.343.3H61.735a.743.743,0,0,0,0,1.486h.229a.375.375,0,0,1,.374.367v8.35A2.085,2.085,0,0,0,64.408,105h6.684a2.086,2.086,0,0,0,2.072-2.095V94.529a.372.372,0,0,1,.368-.34h.233a.743.743,0,0,0,0-1.486Zm-7.954-.608a.609.609,0,0,1,.608-.607h2.667a.6.6,0,0,1,.6.6v.243a.373.373,0,0,1-.357.371H66.168a.373.373,0,0,1-.357-.371Zm5.882,10.811a.61.61,0,0,1-.608.608h-6.67a.608.608,0,0,1-.608-.608V94.564a.375.375,0,0,1,.375-.375h7.136a.375.375,0,0,1,.375.375Z" transform="translate(0)"/><path d="M68.016,98.108a.985.985,0,0,0-.98.99V104.5a.98.98,0,1,0,1.96,0V99.1A.985.985,0,0,0,68.016,98.108Z" transform="translate(-1.693 -3.214)"/><path d="M71.984,98.108a.985.985,0,0,0-.98.99V104.5a.98.98,0,1,0,1.96,0V99.1A.985.985,0,0,0,71.984,98.108Z" transform="translate(-2.807 -3.214)"/></g></svg>
									</a>
								</td>
							</tr>
							<tr :style="$route.params.name == conf.name ? 'display: table-row' : ''" :class="$route.params.name == conf.name ? 'open details pgConfig' : 'details pgConfig'">
								<td colspan="3">
									<div class="configurationDetails" v-if="conf.data.status.clusters.length">
										<span class="title">Configuration Details</span>	
										<table>
											<tbody>
												<tr>
													<td class="label">Used on</td>
													<td class="usedOn">
														<ul>
															<li v-for="c in conf.data.status.clusters">
																{{ c }}
																<router-link :to="'/admin/cluster/status/'+currentNamespace+'/'+c" title="Cluster Details">
																	<svg xmlns="http://www.w3.org/2000/svg" width="18.556" height="14.004" viewBox="0 0 18.556 14.004"><g transform="translate(0 -126.766)"><path d="M18.459,133.353c-.134-.269-3.359-6.587-9.18-6.587S.232,133.084.1,133.353a.93.93,0,0,0,0,.831c.135.269,3.36,6.586,9.18,6.586s9.046-6.317,9.18-6.586A.93.93,0,0,0,18.459,133.353Zm-9.18,5.558c-3.9,0-6.516-3.851-7.284-5.142.767-1.293,3.382-5.143,7.284-5.143s6.516,3.85,7.284,5.143C15.795,135.06,13.18,138.911,9.278,138.911Z" transform="translate(0 0)"/><path d="M9.751,130.857a3.206,3.206,0,1,0,3.207,3.207A3.21,3.21,0,0,0,9.751,130.857Z" transform="translate(-0.472 -0.295)"/></g></svg>
																</router-link>
															</li>
														</ul>
													</td>
												</tr>
											</tbody>
										</table>
									</div>
									<div class="paramDetails" v-if="conf.data.spec.pgBouncer['pgbouncer.ini'].length">
										<template v-if="conf.data.status.pgBouncer['pgbouncer.ini'].length != conf.data.status.pgBouncer.defaultParameters.length">
											<span class="title">Parameters</span>	
											<table>
												<tbody>
													<tr v-for="param in conf.data.status.pgBouncer['pgbouncer.ini']" v-if="!conf.data.status.pgBouncer.defaultParameters.includes(param.parameter)">
														<td class="label">
															{{ param.parameter }}
														</td>
														<td class="paramValue">
															{{ param.value }}
														</td>
													</tr>
												</tbody>
											</table>
										</template>

										<template v-if="conf.data.status.pgBouncer.defaultParameters.length">
											<span class="title">Default Parameters</span>	
											<table>
												<tbody>
													<tr v-for="param in conf.data.status.pgBouncer['pgbouncer.ini']" v-if="conf.data.status.pgBouncer.defaultParameters.includes(param.parameter)">
														<td class="label">
															{{ param.parameter }}
														</td>
														<td class="paramValue">
															{{ param.value }}
														</td>
													</tr>
												</tbody>
											</table>
										</template>
									</div>
								</td>
							</tr>
						</template>
					</tbody>
				</table>
			</div>
			<div id="nameTooltip">
				<div class="info"></div>
			</div>
		</div>`,
	data: function() {
		return {
			currentSort: 'data.metadata.name',
			currentSortDir: 'desc',
		}
	},
	computed: {

		config () {
			return sortTable( store.state.poolConfig, this.currentSort, this.currentSortDir )
		},

		currentNamespace () {
			return store.state.currentNamespace
		},

	},
	mounted: function() {
		$('tr.toggle').click(function() {
			$(this).toggleClass("open");
			$('tr.toggle').not(this).removeClass("open");
		});
	}
})
