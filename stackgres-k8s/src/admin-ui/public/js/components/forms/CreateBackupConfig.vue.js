var CreateBackupConfig = Vue.component("CreateBackupConfig", {
    template: `
        <form id="create-backup-config" v-if="loggedIn && isReady">
            <!-- Vue reactivity hack -->
            <template v-if="Object.keys(config).length > 0"></template>

            <header>
                <ul class="breadcrumbs">
                    <li class="namespace">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20.026" height="27"><g fill="#00adb5"><path d="M1.513.9l-1.5 13a.972.972 0 001 1.1h18a.972.972 0 001-1.1l-1.5-13a1.063 1.063 0 00-1-.9h-15a1.063 1.063 0 00-1 .9zm.6 11.5l.9-8c0-.2.3-.4.5-.4h12.9a.458.458 0 01.5.4l.9 8a.56.56 0 01-.5.6h-14.7a.56.56 0 01-.5-.6zM1.113 17.9a1.063 1.063 0 011-.9h15.8a1.063 1.063 0 011 .9.972.972 0 01-1 1.1h-15.8a1.028 1.028 0 01-1-1.1zM3.113 23h13.8a.972.972 0 001-1.1 1.063 1.063 0 00-1-.9h-13.8a1.063 1.063 0 00-1 .9 1.028 1.028 0 001 1.1zM3.113 25.9a1.063 1.063 0 011-.9h11.8a1.063 1.063 0 011 .9.972.972 0 01-1 1.1h-11.8a1.028 1.028 0 01-1-1.1z"/></g></svg>
                        <router-link :to="'/admin/overview/'+currentNamespace" title="Namespace Overview">{{ currentNamespace }}</router-link>
                    </li>
                    <li class="action">
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 27.1 20"><path d="M15.889 13.75a2.277 2.277 0 011.263.829 2.394 2.394 0 01.448 1.47 2.27 2.27 0 01-.86 1.885 3.721 3.721 0 01-2.375.685h-3.449a.837.837 0 01-.6-.213.8.8 0 01-.22-.6v-7.795a.8.8 0 01.22-.6.835.835 0 01.609-.214h3.306a3.679 3.679 0 012.306.648 2.165 2.165 0 01.836 1.815 2.159 2.159 0 01-.395 1.3 2.254 2.254 0 01-1.089.79zm-4.118-.585h2.179q1.779 0 1.778-1.323a1.143 1.143 0 00-.441-.989 2.267 2.267 0 00-1.337-.321h-2.179zm2.407 4.118a2.219 2.219 0 001.363-.335 1.242 1.242 0 00.428-1.042 1.271 1.271 0 00-.435-1.056 2.155 2.155 0 00-1.356-.348h-2.407v2.781zm8.929 1.457a3.991 3.991 0 01-2.941-1 3.968 3.968 0 01-1-2.927V9.984a.854.854 0 01.227-.622.925.925 0 011.23 0 .854.854 0 01.227.622V14.9a2.623 2.623 0 00.573 1.838 2.18 2.18 0 001.684.622 2.153 2.153 0 001.671-.628 2.624 2.624 0 00.573-1.832V9.984a.85.85 0 01.228-.622.924.924 0 011.229 0 .85.85 0 01.228.622v4.826a3.969 3.969 0 01-1 2.92 3.95 3.95 0 01-2.929 1.01zM.955 4.762h10.5a.953.953 0 100-1.9H.955a.953.953 0 100 1.9zM14.8 7.619a.954.954 0 00.955-.952V4.762h4.3a.953.953 0 100-1.9h-4.3V.952a.955.955 0 00-1.909 0v5.715a.953.953 0 00.954.952zM.955 10.952h4.3v1.9a.955.955 0 001.909 0V7.143a.955.955 0 00-1.909 0v1.9h-4.3a.953.953 0 100 1.9zm6.681 4.286H.955a.953.953 0 100 1.905h6.681a.953.953 0 100-1.905z"></path></svg>
                        <router-link :to="'/admin/configurations/backup/'+currentNamespace" title="SGBackupConfigs">SGBackupConfigs</router-link>
                    </li>
                    <li v-if="editMode">
                        <router-link :to="'/admin/configurations/backup/'+currentNamespace+'/'+$route.params.name" title="Configuration Details">{{ $route.params.name }}</router-link>
                    </li>
                    <li class="action">
                        {{ $route.params.action }}
                    </li>
                </ul>

                <div class="actions">
                    <a class="documentation" href="https://stackgres.io/doc/latest/04-postgres-cluster-management/04-backups/#configuration" target="_blank" title="SGBackupConfig Documentation">SGBackupConfig Documentation</a>
                </div>
            </header>
                    
            <div class="form">
                <div class="header">
                    <h2>Backup Configuration Details</h2>
                    <label for="advancedMode" :class="(advancedMode) ? 'active' : ''">
                        <input v-model="advancedMode" type="checkbox" id="advancedMode" name="advancedMode" />
                        <span>Advanced</span>
                    </label>
                </div>
                
                <label for="metadata.name">Configuration Name <span class="req">*</span></label>
                <input v-model="backupConfigName" :disabled="(editMode)" required data-dield="metadata.name">
                <a class="help" @click="showTooltip( 'SGBackupConfig', 'metadata.name')">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                </a>

                <span class="warning" v-if="nameColission && !editMode">
                    There's already a <strong>SGBackupConfig</strong> with the same name on this namespace. Please specify a different name or create the configuration on another namespace
                </span>

                <fieldset class="cron row-20" data-field="spec.baseBackups.cronSchedule">
                    <div class="header">
                        <h3 for="spec.baseBackups.cronSchedule">Base Backup Schedule <span class="req">*</span></h3>
                    </div>                    
                    
                    <div class="col">
                        <label for="backupConfigFullScheduleMin">Minute <span class="req">*</span></label>
                        <input v-model="backupConfigFullScheduleMin" required>
                    </div>

                    <div class="col">
                        <label for="backupConfigFullScheduleHour">Hour <span class="req">*</span></label>
                        <input v-model="backupConfigFullScheduleHour" required>
                    </div>

                    <div class="col">
                        <label for="backupConfigFullScheduleDOM">Day of Month <span class="req">*</span></label>
                        <input v-model="backupConfigFullScheduleDOM" required>
                    </div>

                    <div class="col">
                        <label for="backupConfigFullScheduleMonth">Month <span class="req">*</span></label>
                        <input v-model="backupConfigFullScheduleMonth" required>
                    </div>

                    <div class="col">
                        <label for="backupConfigFullScheduleDOW">Day of Week <span class="req">*</span></label>
                        <input v-model="backupConfigFullScheduleDOW" required>
                    </div>

                    <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.baseBackups.cronSchedule')">
                        <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                    </a>
                </fieldset>

                <!-- <template v-if="advancedMode">
                    <label for="backupConfigFullWindow">Full Window</label>
                    <input v-model="backupConfigFullWindow" value="" required data-field="spec.fullWindow">
                </template> -->

                <label for="spec.baseBackups.retention">Retention Window (max. number of base backups) <span class="req">*</span></label>
                <input v-model="backupConfigRetention" value="" required data-field="spec.baseBackups.retention" type="number">
                <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.baseBackups.retention')">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                </a>

                <template v-if="advancedMode">
                    <label for="spec.baseBackups.compression">Compression Method</label>
                    <select v-model="backupConfigCompressionMethod" data-field="spec.baseBackups.compression">
                        <option disabled value="">Select a method</option>
                        <option value="lz4">LZ4</option>
                        <option value="lzma">LZMA</option>
                        <option value="brotli">Brotli</option>
                    </select>
                    <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.baseBackups.compression')">
                        <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                    </a>
                </template>

                <template v-if="advancedMode">
                    <label for="spec.baseBackups.performance.maxNetworkBandwitdh">Max Network Bandwitdh</label>
                    <input v-model="backupConfigMaxNetworkBandwitdh" required data-field="spec.baseBackups.performance.maxNetworkBandwitdh" type="number" min="0">
                    <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.baseBackups.performance.maxNetworkBandwitdh')">
                        <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                    </a>
                </template>

                <template v-if="advancedMode">
                    <label for="spec.baseBackups.performance.maxDiskBandwitdh">Max Disk Bandwitdh</label>
                    <input v-model="backupConfigMaxDiskBandwitdh" required data-field="spec.baseBackups.performance.maxDiskBandwitdh" type="number" min="0">
                    <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.baseBackups.performance.maxDiskBandwitdh')">
                        <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                    </a>
                </template>

                <template v-if="advancedMode">
                    <!--<div class="unit-select">
                        <label for="backupConfigTarSizeThreshold">Tar Size Threshold</label>  
                        <input v-model="backupConfigTarSizeThreshold" class="size" value="" data-field="spec.tarSizeThreshold">
                        <select v-model="backupConfigTarSizeThresholdUnit" class="unit" data-field="spec.tarSizeThreshold">
                            <option disabled value="">Select Unit</option>
                            <option value="1024">KiB</option>
                            <option value="1048576">MiB</option>
                            <option value="1073741824">GiB</option>
                            <option value="1099511627776">TiB</option>
                            <option value="1125899906842624">PiB</option>
                            <option value="1152921504606846976">EiB</option>
                            <option value="1180591620717411303424">ZiB</option>
                            <option value="1208925819614629174706176">YiB</option>        
                        </select>
                    </div>-->
                
                    <label for="spec.baseBackups.performance.uploadDiskConcurrency">Upload Disk Concurrency</label>
                    <input v-model="backupConfigUploadDiskConcurrency" value="" data-field="spec.baseBackups.performance.uploadDiskConcurrency" type="number">
                    <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.baseBackups.performance.uploadDiskConcurrency')">
                        <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                    </a>
                </template>

                <label for="spec.storage.type">Storage Type <span class="req">*</span></label>
                <select v-model="backupConfigStorageType" data-field="spec.storage.type">
                    <option disabled value="">Select Storage Type</option>
                    <option value="s3">Amazon S3</option>
                    <option value="s3Compatible">Amazon S3 - API Compatible</option>
                    <option value="gcs">Google Cloud Storage</option>
                    <option value="azureBlob">Azure Blob Storage</option>
                </select>
                <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.type')">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                </a>

                <fieldset class="fieldset" required v-if="backupConfigStorageType.length">
                    <div class="header">
                        <h3 v-if="backupConfigStorageType === 's3'">
                            Amazon S3 Configuration
                        </h3>
                        <h3 v-else-if="backupConfigStorageType === 's3Compatible'">
                            AS3 Compatible Configuration
                        </h3>
                        <h3 v-else-if="backupConfigStorageType === 'gcs'">
                            Google Cloud Storage Configuration
                        </h3>
                        <h3 v-else-if="backupConfigStorageType === 'azureBlob'">
                            Microsoft Azure Configuration
                        </h3>

                        <label for="advancedModeStorage" :class="(advancedModeStorage) ? 'active' : ''">
                            <input v-model="advancedModeStorage" type="checkbox" id="advancedModeStorage"/>
                            <span>Advanced</span>
                        </label>
                    </div>
                    

                    <template v-if="backupConfigStorageType === 's3'">
                        <label for="spec.storage.s3.bucket">Bucket <span class="req">*</span></label>
                        <input v-model="backupS3Bucket" data-field="spec.storage.s3.bucket">
                        <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.s3.bucket')">
                            <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                        </a>

                        <template v-if="advancedModeStorage">
                            <label for="spec.storage.s3.path">Path</label>
                            <input v-model="backupS3Path" data-field="spec.storage.s3.path">
                            <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.s3.path')">
                                <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                            </a>

                            <label for="spec.storage.s3.region">Region</label>
                            <input v-model="backupS3Region" data-field="spec.storage.s3.region">
                            <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.s3.region')">
                                <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                            </a>
                        </template>

                        <label for="spec.storage.s3.awsCredentials.secretKeySelectors.accessKeyId">API Key <span class="req">*</span></label>
                        <input v-model="backupS3AccessKeyId" required data-field="spec.storage.s3.awsCredentials.secretKeySelectors.accessKeyId">
                        <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.s3.awsCredentials.secretKeySelectors.accessKeyId')">
                            <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                        </a>

                        <label for="spec.storage.s3.awsCredentials.secretKeySelectors.secretAccessKey">API Secret <span class="req">*</span></label>
                        <input v-model="backupS3SecretAccessKey" required data-field="spec.storage.s3.awsCredentials.secretKeySelectors.secretAccessKey" type="password">
                        <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.s3.awsCredentials.secretKeySelectors.secretAccessKey')">
                            <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                        </a>

                        <template v-if="advancedModeStorage">
                            <label for="spec.storage.s3.storageClass">Storage Class</label>
                            <select v-model="backupS3StorageClass" data-field="spec.storage.s3.storageClass">
                                <option disabled value="">Select Storage Class...</option>
                                <option value="STANDARD">Standard</option>
                                <option value="STANDARD_IA">Infrequent Access</option>
                                <option value="REDUCED_REDUNDANCY">Reduced Redundancy</option>
                            </select>
                            <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.s3.storageClass')">
                                <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                            </a>
                        </template>
                    </template>

                    <template v-if="backupConfigStorageType === 's3Compatible'">
                        <label for="spec.storage.s3Compatible.bucket">Bucket <span class="req">*</span></label>
                        <input v-model="backupS3CompatibleBucket" data-field="spec.storage.s3Compatible.bucket">
                        <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.s3Compatible.bucket')">
                            <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                        </a>

                        <template v-if="advancedModeStorage">
                            <label for="spec.storage.s3Compatible.path">Path</label>
                            <input v-model="backupS3CompatiblePath" data-field="spec.storage.s3Compatible.path">
                            <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.s3Compatible.path')">
                                <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                            </a>

                            <label for="spec.storage.s3Compatible.endpoint">Endpoint</label>
                            <input v-model="backupS3CompatibleEndpoint" data-field="spec.storage.s3Compatible.endpoint">
                            <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.s3Compatible.endpoint')">
                                <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                            </a>

                            <label for="spec.storage.s3Compatible.region">Region</label>
                            <input v-model="backupS3CompatibleRegion" data-field="spec.storage.s3Compatible.region">
                            <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.s3Compatible.region')">
                                <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                            </a>
                        </template>

                        <label for="spec.storage.s3Compatible.awsCredentials.secretKeySelectors.accessKeyId">API Key <span class="req">*</span></label>
                        <input v-model="backupS3CompatibleAccessKeyId" required data-field="spec.storage.s3Compatible.awsCredentials.secretKeySelectors.accessKeyId">
                        <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.s3Compatible.awsCredentials.secretKeySelectors.accessKeyId')">
                            <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                        </a>

                        <label for="spec.storage.s3Compatible.awsCredentials.secretKeySelectors.secretAccessKey">API Secret <span class="req">*</span></label>
                        <input v-model="backupS3CompatibleSecretAccessKey" required data-field="spec.storage.s3Compatible.awsCredentials.secretKeySelectors.secretAccessKey" type="password">
                        <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.s3Compatible.awsCredentials.secretKeySelectors.secretAccessKey')">
                            <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                        </a>

                        <template v-if="advancedModeStorage">
                            <label for="spec.storage.s3Compatible.enablePathStyleAddressing">Enable Path Style Addressing</label>
                            <label for="backupS3CompatibleEnablePathStyleAddressing" class="switch">
                                Bucket URL Force Path Style
                                <input type="checkbox" id="enablePathStyleAddressing" v-model="backupS3CompatibleEnablePathStyleAddressing" data-switch="OFF" data-field="spec.storage.s3Compatible.enablePathStyleAddressing">
                            </label>
                            <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.s3Compatible.enablePathStyleAddressing')">
                                <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                            </a>
                        
                            <label for="spec.storage.s3Compatible.storageClass">Storage Class</label>
                            <select v-model="backupS3CompatibleStorageClass" data-field="spec.storage.s3Compatible.storageClass">
                                <option disabled value="">Select Storage Class...</option>
                                <option value="STANDARD">Standard</option>
                                <option value="STANDARD_IA">Infrequent Access</option>
                                <option value="REDUCED_REDUNDANCY">Reduced Redundancy</option>
                            </select>
                            <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.s3Compatible.storageClass')">
                                <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                            </a>
                        </template>
    
                    </template>

                    <template v-if="backupConfigStorageType === 'gcs'">
                        <label for="spec.storage.gcs.bucket">Bucket <span class="req">*</span></label>
                        <input v-model="backupGCSBucket" data-field="spec.storage.gcs.bucket">
                        <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.gcs.bucket')">
                            <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                        </a>

                        <template v-if="advancedModeStorage">
                            <label for="spec.storage.gcs.path">Path</label>
                            <input v-model="backupGCSPath" data-field="spec.storage.gcs.path">
                            <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.gcs.path')">
                                <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                            </a>
                        </template>

                        <label for="spec.storage.gcs.gcpCredentials.fetchCredentialsFromMetadataService">Fetch Credentials from Metadata Service</label>  
                        <label for="fetchGCSCredentials" class="switch yes-no">Fetch <input type="checkbox" id="fetchGCSCredentials" v-model="fetchGCSCredentials" data-switch="NO"></label>
                        <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.gcs.gcpCredentials.fetchCredentialsFromMetadataService')">
                            <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                        </a>

                        <template v-if="!fetchGCSCredentials">
                            <label for="spec.storage.gcs.gcpCredentials.secretKeySelectors.serviceAccountJSON">Service Account JSON <span class="req">*</span></label>
                            <input id="uploadJSON" type="file" @change="uploadJSON" :required="!editMode" data-field="spec.storage.gcs.gcpCredentials.secretKeySelectors.serviceAccountJSON">
                            <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.gcs.gcpCredentials.secretKeySelectors.serviceAccountJSON')">
                                <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                            </a>
                            <textarea id="textJSON" v-model="backupGCSServiceAccountJSON" data-field="spec.storage.gcs.gcpCredentials.serviceAccountJSON" class="hide"></textarea>
                        </template>
                    </template>

                    <template v-if="backupConfigStorageType === 'azureBlob'">
                        <label for="spec.storage.azureBlob.bucket">Bucket <span class="req">*</span></label>
                        <input v-model="backupAzureBucket" data-field="spec.storage.azureBlob.bucket">
                        <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.azureBlob.bucket')">
                            <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                        </a>

                        <template v-if="advancedModeStorage">
                            <label for="spec.storage.azureBlob.path">Path</label>
                            <input v-model="backupAzurePath" data-field="spec.storage.azureBlob.path">
                            <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.azureBlob.path')">
                                <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                            </a>
                        </template>

                        <label for="spec.storage.azureBlob.azureCredentials.secretKeySelectors.storageAccount">Account Name <span class="req">*</span></label>
                        <input v-model="backupAzureAccount" required data-field="spec.storage.azureBlob.azureCredentials.secretKeySelectors.storageAccount">
                        <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.azureBlob.azureCredentials.secretKeySelectors.storageAccount')">
                            <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                        </a>

                        <label for="spec.storage.azureBlob.azureCredentials.secretKeySelectors.accessKey">Account Access Key <span class="req">*</span></label>
                        <input v-model="backupAzureAccessKey" required data-field="spec.storage.azureBlob.azureCredentials.secretKeySelectors.accessKey" type="password">
                        <a class="help" @click="showTooltip( 'SGBackupConfig', 'spec.storage.azureBlob.azureCredentials.secretKeySelectors.accessKey')">
                            <svg xmlns="http://www.w3.org/2000/svg" width="14.993" height="14.993" viewBox="0 0 14.993 14.993"><path d="M75.9-30a7.5,7.5,0,0,0-7.5,7.5,7.5,7.5,0,0,0,7.5,7.5,7.5,7.5,0,0,0,7.5-7.5A7.5,7.5,0,0,0,75.9-30Z" transform="translate(-68.4 30)" fill="#7a7b85"/><g transform="translate(4.938 3.739)"><path d="M78.008-17.11a.881.881,0,0,0-.629.248.833.833,0,0,0-.259.612.819.819,0,0,0,.271.653.906.906,0,0,0,.6.224H78a.864.864,0,0,0,.6-.226.813.813,0,0,0,.267-.639.847.847,0,0,0-.25-.621A.9.9,0,0,0,78.008-17.11Z" transform="translate(-75.521 23.034)" fill="#fff"/><path d="M79.751-23.993a2.13,2.13,0,0,0-.882-.749,3.07,3.07,0,0,0-1.281-.27,2.978,2.978,0,0,0-1.376.322,2.4,2.4,0,0,0-.906.822,1.881,1.881,0,0,0-.318,1v.009a.734.734,0,0,0,.231.511.762.762,0,0,0,.549.238h.017a.778.778,0,0,0,.767-.652,1.92,1.92,0,0,1,.375-.706.871.871,0,0,1,.668-.221.891.891,0,0,1,.618.22.687.687,0,0,1,.223.527.572.572,0,0,1-.073.283,1.194,1.194,0,0,1-.2.265c-.088.088-.232.22-.43.394a7.645,7.645,0,0,0-.565.538,1.905,1.905,0,0,0-.356.566,1.893,1.893,0,0,0-.134.739.8.8,0,0,0,.217.607.751.751,0,0,0,.519.206h.046a.689.689,0,0,0,.454-.171.662.662,0,0,0,.229-.452c.031-.149.055-.255.073-.315a.827.827,0,0,1,.061-.153.878.878,0,0,1,.124-.175,3.05,3.05,0,0,1,.246-.247c.39-.345.665-.6.818-.75a2.3,2.3,0,0,0,.42-.565,1.635,1.635,0,0,0,.183-.782A1.859,1.859,0,0,0,79.751-23.993Z" transform="translate(-74.987 25.012)" fill="#fff"/></g></svg>
                        </a>
                    </template>
                </fieldset>

                
                <a class="btn" @click="createBackupConfig">
                    <template v-if="editMode">Update Configuration</template>
                    <template v-else>Create Configuration</template>
                </a>

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
            advancedMode: false,
            advancedModeStorage: false,
            backupConfigName: vm.$route.params.hasOwnProperty('name') ? vm.$route.params.name : '',
            backupConfigNamespace: vm.$route.params.hasOwnProperty('namespace') ? vm.$route.params.namespace : '',
            backupConfigCompressionMethod: 'lz4',
            backupConfigFullSchedule: '*/2 * * * *',
            backupConfigFullScheduleMin: '*/2',
            backupConfigFullScheduleHour: '*',
            backupConfigFullScheduleDOM: '*',
            backupConfigFullScheduleMonth: '*',
            backupConfigFullScheduleDOW: '*',
            backupConfigRetention: 5,
            backupConfigTarSizeThreshold: 1,
            backupConfigTarSizeThresholdUnit: 1073741824,
            backupConfigMaxNetworkBandwitdh: '',
            backupConfigMaxDiskBandwitdh: '',
            backupConfigUploadDiskConcurrency: 1,
            backupConfigStorageType: '',
            backupS3Bucket: '',
            backupS3Path: '',
            backupS3Region: '',
            backupS3AccessKeyId: '',
            backupS3SecretAccessKey: '',
            backupS3StorageClass: '',
            backupS3CompatibleBucket: '',
            backupS3CompatiblePath: '',
            backupS3CompatibleEndpoint: '',
            backupS3CompatibleRegion: '',
            backupS3CompatibleAccessKeyId: '',
            backupS3CompatibleSecretAccessKey: '',
            backupS3CompatibleStorageClass: '',
            backupS3CompatibleEnablePathStyleAddressing: false,
            backupGCSBucket: '',
            backupGCSPath: '',
            fetchGCSCredentials: false,
            backupGCSServiceAccountJSON: '',
            backupAzureBucket: '',
            backupAzurePath: '',
            backupAzureAccount: '',
            backupAzureAccessKey: '',
        }
            
	},
	computed: {
        allNamespaces () {
            return store.state.allNamespaces
        },

        currentNamespace() {
            return store.state.currentNamespace
        },

        tooltips() {
            return store.state.tooltips.description
        },

        nameColission() {

            const vc = this;
            var nameColission = false;
            
            store.state.backupConfig.forEach(function(item, index){
				if( (item.name == vc.backupConfigName) && (item.data.metadata.namespace == store.state.currentNamespace ) )
					nameColission = true
			})

			return nameColission
        },

        config() {

            var vm = this;
            var conf = {};
            
            if(vm.$route.params.action === 'edit') {
                store.state.backupConfig.forEach(function( config ){
                    if( (config.data.metadata.name === vm.$route.params.name) && (config.data.metadata.namespace === vm.$route.params.namespace) ) {
                        
                        let tresholdSize = formatBytes(config.data.spec.tarSizeThreshold);
                        let tresholdUnit = '';            
                        let unitSizes = {
                            "Ki": 1024, 
                            "Mi": 1048576,
                            "Gi": 1073741824,
                            "Ti": 1099511627776,
                            "Pi": 1125899906842624,
                            "Ei": 1152921504606846976,
                            "Zi": 1180591620717411303424,
                            "Yi": 1208925819614629174706176
                        };
                        
                        $.each( unitSizes, function( index, value ){
                            if( tresholdSize.match(/[a-zA-Z]+/g)[0] === index ) {
                                tresholdUnit = value;
                                return false;
                            }
                        });
                        
                        // console.log(tresholdSize);
                        // console.log(tresholdSize.match(/[a-zA-Z]+/g));
                        // console.log(tresholdSize.match(/[a-zA-Z]+/g)[0]);
    
                        // Cron to Human
                        let cron = config.data.spec.baseBackups.cronSchedule.split(" ");
    
                        vm.backupConfigCompressionMethod = config.data.spec.baseBackups.compression;
                        vm.backupConfigFullSchedule = config.data.spec.baseBackups.cronSchedule
                        vm.backupConfigFullScheduleMin = cron[0];
                        vm.backupConfigFullScheduleHour = cron[1];
                        vm.backupConfigFullScheduleDOM = cron[2];
                        vm.backupConfigFullScheduleMonth = cron[3];
                        vm.backupConfigFullScheduleDOW = cron[4];
                        //backupConfigFullWindow = config.data.spec.fullWindow;
                        vm.backupConfigRetention = config.data.spec.baseBackups.retention;
                        vm.backupConfigTarSizeThreshold = tresholdSize.match(/\d+/g);
                        vm.backupConfigTarSizeThresholdUnit = tresholdUnit;
                        vm.backupConfigMaxNetworkBandwitdh = config.data.spec.baseBackups.performance.maxNetworkBandwitdh;
                        vm.backupConfigMaxDiskBandwitdh = config.data.spec.baseBackups.performance.maxDiskBandwitdh;
                        vm.backupConfigUploadDiskConcurrency = config.data.spec.baseBackups.performance.uploadDiskConcurrency;
                        vm.backupConfigStorageType = config.data.spec.storage.type;
    
                        //s3
                        if(config.data.spec.storage.type === 's3') {
                            vm.backupS3Bucket = config.data.spec.storage.s3.bucket;
                            vm.backupS3Path =  (typeof config.data.spec.storage.s3.path !== 'undefined') ? config.data.spec.storage.s3.path : '';
                            vm.backupS3Region =  (typeof config.data.spec.storage.s3.region !== 'undefined') ? config.data.spec.storage.s3.region : '';
                            vm.backupS3AccessKeyId = config.data.spec.storage.s3.awsCredentials.accessKeyId;
                            vm.backupS3SecretAccessKey = config.data.spec.storage.s3.awsCredentials.secretAccessKey;
                            vm.backupS3StorageClass = (typeof config.data.spec.storage.s3.storageClass !== 'undefined') ? config.data.spec.storage.s3.storageClass : '';
                        }
                        
                        //s3Compatible
                        if(config.data.spec.storage.type === 's3Compatible') {
                            vm.backupS3CompatibleBucket = config.data.spec.storage.s3Compatible.bucket;
                            vm.backupS3CompatiblePath = (typeof config.data.spec.storage.s3Compatible.path !== 'undefined') ? config.data.spec.storage.s3Compatible.path : '';
                            vm.backupS3CompatibleEndpoint = (typeof config.data.spec.storage.s3Compatible.endpoint !== 'undefined') ? config.data.spec.storage.s3Compatible.endpoint : '';
                            vm.backupS3CompatibleRegion = (typeof config.data.spec.storage.s3Compatible.region !== 'undefined') ? config.data.spec.storage.s3Compatible.region : '';
                            vm.backupS3CompatibleAccessKeyId = config.data.spec.storage.s3Compatible.awsCredentials.accessKeyId;
                            vm.backupS3CompatibleSecretAccessKey = config.data.spec.storage.s3Compatible.awsCredentials.secretAccessKey;
                            vm.backupS3CompatibleStorageClass = (typeof config.data.spec.storage.s3Compatible.storageClass !== 'undefined') ? config.data.spec.storage.s3Compatible.storageClass : '';
                            vm.backupS3CompatibleEnablePathStyleAddressing = config.data.spec.storage.s3Compatible.enablePathStyleAddressing;
                        }
                        
                        //gcs
                        if(config.data.spec.storage.type === 'gcs') {
                            vm.backupGCSBucket = config.data.spec.storage.gcs.bucket;
                            vm.backupGCSPath = (typeof config.data.spec.storage.gcs.path !== 'undefined') ? config.data.spec.storage.gcs.path : '';
                            vm.fetchGCSCredentials = hasProp(config, 'data.spec.storage.gcs.gcpCredentials.fetchCredentialsFromMetadataService') ? config.data.spec.storage.gcs.gcpCredentials.fetchCredentialsFromMetadataService : false ;
                            vm.backupGCSServiceAccountJSON = hasProp(config, 'data.spec.storage.gcs.gcpCredentials.serviceAccountJSON') ? config.data.spec.storage.gcs.gcpCredentials.serviceAccountJSON : '';
                        }
                        
                        //azure
                        if(config.data.spec.storage.type === 'azureBlob') {
                            vm.backupAzureBucket = config.data.spec.storage.azureBlob.bucket;
                            vm.backupAzurePath = (typeof config.data.spec.storage.azureBlob.path !== 'undefined') ? config.data.spec.storage.azureBlob.path : '';
                            vm.backupAzureAccount = config.data.spec.storage.azureBlob.azureCredentials.storageAccount;
                            vm.backupAzureAccessKey = config.data.spec.storage.azureBlob.azureCredentials.accessKey;
                        }
    
                        conf = config;
                        return false;
                    }
                });
            } 
        
            return conf

        }
    },
    methods: {

        
        createBackupConfig: function(e) {
            //e.preventDefault();

            let isValid = true;
            
            $('input:required, select:required').each(function() {
                if ($(this).val() === '') {
                    isValid = false;
                    return false;
                }
                    
            });

            if(isValid) {
                let storage = {};

                switch(this.backupConfigStorageType) {
                    
                    case 's3':
                        storage['s3'] = {
                            "bucket": this.backupS3Bucket,
                            ...( ((typeof this.backupS3Path !== 'undefined') && this.backupS3Path.length ) && ( {"path": this.backupS3Path }) ),
                            ...( ((typeof this.backupS3Region !== 'undefined') && this.backupS3Region.length ) && ( {"region": this.backupS3Region }) ),
                            ...( ((typeof this.backupS3StorageClass !== 'undefined') && this.backupS3StorageClass.length ) && ( {"storageClass": this.backupS3StorageClass }) ),
                            "awsCredentials": {
                                "accessKeyId": this.backupS3AccessKeyId,
                                "secretAccessKey": this.backupS3SecretAccessKey
                            }
                        };
                        storage['type'] = 's3';
                        break;
                    
                    case 's3Compatible':
                        storage['s3Compatible'] = {
                            "bucket": this.backupS3CompatibleBucket,
                            ...( ((typeof this.backupS3CompatiblePath !== 'undefined') && this.backupS3CompatiblePath.length ) && ( {"path": this.backupS3CompatiblePath }) ),
                            ...( ((typeof this.backupS3CompatibleEnablePathStyleAddressing !== 'undefined') && this.backupS3CompatibleEnablePathStyleAddressing ) && ( {"enablePathStyleAddressing": this.backupS3CompatibleEnablePathStyleAddressing }) ),
                            ...( ((typeof this.backupS3CompatibleEndpoint !== 'undefined') && this.backupS3CompatibleEndpoint.length ) && ( {"endpoint": this.backupS3CompatibleEndpoint }) ),
                            ...( ((typeof this.backupS3CompatibleRegion !== 'undefined') && this.backupS3CompatibleRegion.length ) && ( {"region": this.backupS3CompatibleRegion }) ),
                            ...( ((typeof this.backupS3CompatibleStorageClass !== 'undefined') && this.backupS3CompatibleStorageClass.length ) && ( {"storageClass": this.backupS3CompatibleStorageClass }) ),
                            "awsCredentials": {
                                "accessKeyId": this.backupS3CompatibleAccessKeyId,
                                "secretAccessKey": this.backupS3CompatibleSecretAccessKey
                            },
                        };
                        storage['type'] = 's3Compatible';
                        break;

                    case 'gcs':
                        storage['gcs'] = {
                            "bucket": this.backupGCSBucket,
                            ...( ((typeof this.backupGCSPath !== 'undefined') && this.backupGCSPath.length ) && ( {"path": this.backupGCSPath }) ),
                            "gcpCredentials": {
                                ...( this.fetchGCSCredentials && {
                                    "fetchCredentialsFromMetadataService": true
                                }),
                                ...( !this.fetchGCSCredentials && {
                                    "serviceAccountJSON": this.backupGCSServiceAccountJSON
                                })
                            },                            
                        }
                        storage['type'] = 'gcs';
                        break;

                    case 'azureBlob':
                        storage['azureBlob'] = {
                            "bucket": this.backupAzureBucket,
                            ...( ((typeof this.backupAzurePath !== 'undefined') && this.backupAzurePath.length ) && ( {"path": this.backupAzurePath }) ),
                            "azureCredentials": {
                                "storageAccount": this.backupAzureAccount,
                                "accessKey": this.backupAzureAccessKey
                            }
                        }
                        storage['type'] = 'azureBlob';
                        break;

                }

                let config = { 
                    "metadata": {
                        "name": this.backupConfigName,
                        "namespace": this.backupConfigNamespace
                    },
                    "spec": {
                        "baseBackups": {
                            "cronSchedule": this.backupConfigFullScheduleMin+' '+this.backupConfigFullScheduleHour+' '+this.backupConfigFullScheduleDOM+' '+this.backupConfigFullScheduleMonth+' '+this.backupConfigFullScheduleDOW,
                            "retention": this.backupConfigRetention,
                            ...( ((typeof this.backupConfigCompressionMethod !== 'undefined') && this.backupConfigCompressionMethod.length ) && ( {"compression": this.backupConfigCompressionMethod }) ),
                            "performance": {
                                "uploadDiskConcurrency": this.backupConfigUploadDiskConcurrency,
                                "maxNetworkBandwitdh": this.backupConfigMaxNetworkBandwitdh,
                                "maxDiskBandwitdh": this.backupConfigMaxDiskBandwitdh
                            }
                        },
                        "storage": storage
                    }
                }

                //console.log(config)

                if(this.editMode) {
                    
                    const res = axios
                    .put(
                        apiURL+'sgbackupconfig/', 
                        config 
                    )
                    .then(function (response) {
                        notify('Backup configuration <strong>"'+config.metadata.name+'"</strong> updated successfully', 'message','sgbackupconfig');

                        vm.fetchAPI('sgbackupconfig');
                        router.push('/admin/configurations/backup/'+config.metadata.namespace+'/'+config.metadata.name);
                    })
                    .catch(function (error) {
                        console.log(error.response);
                        notify(error.response.data,'error','sgbackupconfig');
                    });

                } else {
                    const res = axios
                    .post(
                        apiURL+'sgbackupconfig/', 
                        config 
                    )
                    .then(function (response) {
                        notify('Backup configuration <strong>"'+config.metadata.name+'"</strong> created successfully', 'message','sgbackupconfig');

                        vm.fetchAPI('sgbackupconfig');
                        router.push('/admin/configurations/backup/'+config.metadata.namespace+'/'+config.metadata.name);
                        

                        /* store.commit('updateBackupConfig', { 
                            name: config.metadata.name,
                            data: config
                        }); */

                        setTimeout(function(){
                            $(".backupConfig").val(config.metadata.name).change().focus();
                            hideFields('.backup-config');
                        },1000);
                    })
                    .catch(function (error) {
                        console.log(error.response);
                        notify(error.response.data,'error','sgbackupconfig');
                    });
                }

            }

        },

        cancel: function() {
            router.push('/admin/configurations/backup/'+store.state.currentNamespace);
        },

        showFields: function( fields ) {
            $(fields).slideDown();
        },

        hideFields: function( fields ) {
            $(fields).slideUp();
        },
        
        uploadJSON: function(e) {
            var files = e.target.files || e.dataTransfer.files;
            let vm = this;

            if (!files.length){
                console.log("File not loaded")
                return;
            } else {
                console.log("File loaded");

                var reader = new FileReader();
                
                reader.onload = function(e) {
                  vm.backupGCSServiceAccountJSON = e.target.result;
                };
                reader.readAsText(files[0]);
            }
        }

    },
    created: function() {

        this.loadTooltips('SGBackupConfig');

    },

    mounted: function() {
    },

    beforeDestroy: function() {
        store.commit('setTooltipDescription','Click on a question mark to get help and tips about that field.');
    }
})