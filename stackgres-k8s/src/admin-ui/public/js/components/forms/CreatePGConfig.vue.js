var CreatePGConfig = Vue.component("CreatePgConfig", {
    template: `
        <form id="create-pgconfig" class="noSubmit" v-if="loggedIn && isReady">
            <!-- Vue reactivity hack -->
            <template v-if="Object.keys(config).length > 0"></template>
            <header>
                <ul class="breadcrumbs">
                    <li class="namespace">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20.026" height="27"><g fill="#00adb5"><path d="M1.513.9l-1.5 13a.972.972 0 001 1.1h18a.972.972 0 001-1.1l-1.5-13a1.063 1.063 0 00-1-.9h-15a1.063 1.063 0 00-1 .9zm.6 11.5l.9-8c0-.2.3-.4.5-.4h12.9a.458.458 0 01.5.4l.9 8a.56.56 0 01-.5.6h-14.7a.56.56 0 01-.5-.6zM1.113 17.9a1.063 1.063 0 011-.9h15.8a1.063 1.063 0 011 .9.972.972 0 01-1 1.1h-15.8a1.028 1.028 0 01-1-1.1zM3.113 23h13.8a.972.972 0 001-1.1 1.063 1.063 0 00-1-.9h-13.8a1.063 1.063 0 00-1 .9 1.028 1.028 0 001 1.1zM3.113 25.9a1.063 1.063 0 011-.9h11.8a1.063 1.063 0 011 .9.972.972 0 01-1 1.1h-11.8a1.028 1.028 0 01-1-1.1z"/></g></svg>
                        <router-link :to="'/admin/overview/'+$route.params.namespace" title="Namespace Overview">{{ $route.params.namespace }}</router-link>
                    </li>
                    <li class="action">
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 26.7 20"><path d="M10.946 18.7a.841.841 0 01-.622-.234.862.862 0 01-.234-.635v-7.817a.8.8 0 01.221-.6.834.834 0 01.608-.214h3.29a3.4 3.4 0 012.353.755 2.7 2.7 0 01.843 2.12 2.72 2.72 0 01-.843 2.126 3.379 3.379 0 01-2.353.764h-2.394v2.875a.8.8 0 01-.869.867zM14 13.637q1.778 0 1.778-1.551T14 10.535h-2.18v3.1zm11.968-.107a.683.683 0 01.494.181.625.625 0 01.191.477v2.875a1.717 1.717 0 01-.16.87 1.174 1.174 0 01-.655.414 6.882 6.882 0 01-1.242.294 9.023 9.023 0 01-1.364.107 5.252 5.252 0 01-2.527-.573 3.883 3.883 0 01-1.638-1.665 5.548 5.548 0 01-.569-2.6 5.5 5.5 0 01.569-2.575 3.964 3.964 0 011.611-1.671 4.965 4.965 0 012.455-.59 4.62 4.62 0 013.089 1.016 1.058 1.058 0 01.234.294.854.854 0 01-.087.843.479.479 0 01-.388.2.737.737 0 01-.267-.047 1.5 1.5 0 01-.281-.153 4.232 4.232 0 00-1.1-.582 3.648 3.648 0 00-1.146-.167 2.747 2.747 0 00-2.2.859 3.834 3.834 0 00-.742 2.561q0 3.477 3.049 3.477a6.752 6.752 0 001.815-.254v-2.36h-1.517a.737.737 0 01-.5-.161.664.664 0 010-.909.732.732 0 01.5-.161zM.955 4.762h10.5a.953.953 0 100-1.9H.955a.953.953 0 100 1.9zM14.8 7.619a.954.954 0 00.955-.952V4.762h4.3a.953.953 0 100-1.9h-4.3V.952a.955.955 0 00-1.909 0v5.715a.953.953 0 00.954.952zM.955 10.952h4.3v1.9a.955.955 0 001.909 0V7.143a.955.955 0 00-1.909 0v1.9h-4.3a.953.953 0 100 1.9zm6.681 4.286H.955a.953.953 0 100 1.905h6.681a.953.953 0 100-1.905z" class="a"></path></svg>
                        <router-link :to="'/admin/configurations/postgres/'+$route.params.namespace" title="SGPostgresConfigs">SGPostgresConfigs</router-link>
                    </li>
                    <li v-if="editMode">
                        <router-link :to="'/admin/configurations/postgres/'+$route.params.namespace+'/'+$route.params.name" title="Configuration Details">{{ $route.params.name }}</router-link>
                    </li>
                    <li class="action">
                        {{ $route.params.action }}
                    </li>
                </ul>

                <div class="actions">
                    <a class="documentation" href="https://stackgres.io/doc/latest/04-postgres-cluster-management/02-configuration-tuning/02-postgres-configuration/" target="_blank" title="SGPostgresConfig Documentation">SGPostgresConfig Documentation</a>
                </div>
            </header>

            <div class="form">
                <div class="header">
                    <h2>Postgres Configuration Details</h2>
                </div>

                <label for="metadata.name">Configuration Name <span class="req">*</span></label>
                <input v-model="pgConfigName" :disabled="(editMode)" required data-field="metadata.name">
                <a class="help" @click="showTooltip( 'SGPostgresConfig', 'metadata.name')">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                </a>

                <span class="warning" v-if="nameColission && !editMode">
                    There's already a <strong>SGPostgresConfig</strong> with the same name on this namespace. Please specify a different name or create the configuration on another namespace
                </span>

                <label for="spec.postgresVersion">Postgres Version <span class="req">*</span></label>
                <select v-model="pgConfigVersion" :disabled="(editMode)" required data-field="spec.postgresVersion">
                    <option disabled value="">Select Major Postgres Version</option>
                    <option value="11">11</option>
                    <option value="12">12</option>
                </select>
                <a class="help" @click="showTooltip( 'SGPostgresConfig', 'spec.postgresVersion')">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                </a>

                <label for="spec.postgresql.conf">Parameters</label>
                <textarea v-model="pgConfigParams" placeholder="parameter = value" data-field="spec.postgresql.conf"></textarea>
                <a class="help" @click="showTooltip( 'SGPostgresConfig', 'spec.postgresql.conf')">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                </a>

                <template v-if="editMode">
                    <a class="btn" @click="createPGConfig">Update Configuration</a>
                </template>
                <template v-else>
                    <a class="btn" @click="createPGConfig">Create Configuration</a>
                </template>
                
                <a class="btn border" @click="cancel">Cancel</a>
            </div>
            <div id="help" class="form">
                <div class="header">
                    <h2>Help</h2>
                </div>
                
                <div class="info">
                    <h3 class="title"></h3>
                    <vue-markdown :source=tooltips></vue-markdown>
                </div>
            </div>
        </form>`,
	data: function() {

        const vm = this;

        return {
            editMode: (vm.$route.params.action === 'edit'),
            pgConfigName: vm.$route.params.hasOwnProperty('name') ? vm.$route.params.name : '',
            pgConfigNamespace: vm.$route.params.hasOwnProperty('namespace') ? vm.$route.params.namespace : '',
            pgConfigParams: '',
            pgConfigVersion: '',
        }
        
	},
	computed: {
        allNamespaces () {
            return store.state.allNamespaces
        },

        currentNamespace () {
            return store.state.currentNamespace
        },

        tooltips() {
            return store.state.tooltips.description
        },

        nameColission() {
            const vc = this;
            var nameColission = false;
            
            store.state.pgConfig.forEach(function(item, index) {
				if( (item.name == vc.pgConfigName) && (item.data.metadata.namespace == store.state.currentNamespace ) )
					nameColission = true
			})

			return nameColission
        },

        config() {
            var vm = this;
            var config = {};

            if(vm.$route.params.action === 'edit') {
                store.state.pgConfig.forEach(function( conf ){
                    if( (conf.data.metadata.name === vm.$route.params.name) && (conf.data.metadata.namespace === vm.$route.params.namespace) ) {
                        vm.pgConfigVersion = conf.data.spec.postgresVersion;
                        vm.pgConfigParams = conf.data.spec["postgresql.conf"];
                        config = conf;
                        return false;
                    }
                });    
            }

            return config
        }
    },
    methods: {

        createPGConfig: function(e) {
            //e.preventDefault();

            let isValid = true;
            
            $('input:required, select:required').each(function() {
                if ($(this).val() === '') {
                    isValid = false;
                    return false;
                }
                    
            });

            if(isValid) {
                var config = { 
                    "metadata": {
                        "name": this.pgConfigName,
                        "namespace": this.pgConfigNamespace
                    },
                    "spec": {
                        "postgresVersion": this.pgConfigVersion,
                        "postgresql.conf": this.pgConfigParams
                    }
                }

                if(this.editMode) {
                    const res = axios
                    .put(
                        apiURL+'sgpgconfig/', 
                        config 
                    )
                    .then(function (response) {
                        notify('Postgres configuration <strong>"'+config.metadata.name+'"</strong> updated successfully', 'message', 'sgpgconfig');

                        vm.fetchAPI('sgpgconfig');
                        router.push('/admin/configurations/postgres/'+config.metadata.namespace+'/'+config.metadata.name);
                    })
                    .catch(function (error) {
                        console.log(error.response);
                        notify(error.response.data,'error', 'sgpgconfig');
                    });
                } else {
                    const res = axios
                    .post(
                        apiURL+'sgpgconfig/', 
                        config 
                    )
                    .then(function (response) {
                        notify('Postgres configuration <strong>"'+config.metadata.name+'"</strong> created successfully', 'message', 'sgpgconfig');
        
                        vm.fetchAPI('sgpgconfig');
                        router.push('/admin/configurations/postgres/'+config.metadata.namespace+'/'+config.metadata.name);
                        
                        /* store.commit('updatePGConfig', { 
                            name: config.metadata.name,
                            data: config
                        }); */
                
                    })
                    .catch(function (error) {
                        console.log(error.response);
                        notify(error.response.data,'error', 'sgpgconfig');
                    });
                }
            }

        },

        cancel: function() {
            router.push('/admin/configurations/postgres/'+store.state.currentNamespace);
        },

        showFields: function( fields ) {
            $(fields).slideDown();
        },

        hideFields: function( fields ) {
            $(fields).slideUp();
        }

    },
    created: function() {
        
        this.loadTooltips('SGPostgresConfig');

    },

    mounted: function() {
        
    },

    beforeDestroy: function() {
        store.commit('setTooltipDescription','Click on a question mark to get help and tips about that field.');
    }
})