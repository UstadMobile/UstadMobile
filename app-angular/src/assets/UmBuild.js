/**
 * Script responsible for building, packaging, install and uninstall dependencies.
 * 
 * Since location of all offline dependencies differ from one machine to another, it is better to uninstall all
 * offline dependencies before doing npm install followed by installing module dependenices.
 */

const path = require('path');
const fs = require('fs');
let cp = require("child_process");
const dependencies = [];
let moduleList = null;
const packages = [];

/**
 * Create list of all packages to be installed
 */
function getModuleList(){
    const moduleDir = path.resolve('../build/js/node_modules/')
    const modules = []
    fs.readdirSync(moduleDir).forEach(file => {
        const jsonPackage = path.resolve(moduleDir+'/'+file+'/package.json');
        const fileData = fs.readFileSync(jsonPackage);
            dependencies.push(JSON.parse(fileData).name);
        if(fs.existsSync(jsonPackage)  && !file.includes("UstadMobile-core")
        && !file.includes("UstadMobile-lib-util")){
            modules.push(path.resolve(moduleDir+'/'+file))
        }
      });
      modules.splice(0,0,path.resolve(moduleDir+'/UstadMobile-lib-util'))
      modules.splice(0,0,path.resolve(moduleDir+'/UstadMobile-core'));
      return modules
}

/**
 * Prepare package.json for use, remove all offline dependencies and install default app dependencies
 */
function prepareJsonPackageFile(){
    const packagePath = path.resolve(path.resolve('./')+'/package.json');
    const packageJson = JSON.parse(fs.readFileSync(packagePath)) 
    const dependencyList = packageJson.dependencies
    Object.keys(dependencyList).forEach(key => {
        const value = dependencyList[key]
        if(value.toString().includes("file:../build/js/node_modules/")){
            delete dependencyList[key]
        }
    })
    
    packageJson.dependencies = dependencyList
    fs.writeFileSync(packagePath, JSON.stringify(packageJson,null, 2));
    cp.execSync("sudo npm install", {cwd: path.resolve("./")});
    generateCoreModules();
}


/**
 * Create offline offline NPM packages
 * @param {*} modulePath path of the module
 */
function createPackage(modulePath){
    cp.exec("sudo npm pack", {cwd: modulePath}, function(error,stdout,stderr){
        const package = stdout.replace('\n','')
        console.log(error == null ?  "Packaged ====> "+package : stderr)
        packages.push(path.resolve(modulePath+'/'+package))
        if(moduleList.length != 0){
            createPackage(moduleList.pop())
        }else{
            console.log("Installing dependencies.......")
            let result = cp.execSync("sudo npm install "+packages.join(" "), {cwd: path.resolve("./")});
            console.log(result.toString())

            result = cp.execSync("./gradlew app-angular:generateOtherXliff ", {cwd: path.resolve("../")});
            console.log(result.toString())
        }
        
    });
}

/**
 * Generate JS core modules from source and start packing them 
 */
function generateCoreModules(){
    console.log("Building :core.....")
    const result = cp.execSync("./gradlew core:build ", {cwd: path.resolve("../")});
    console.log(result.toString())
    if(result.toString().includes("BUILD SUCCESSFUL")){
        console.log("Packaging .........");
        moduleList = getModuleList();
        createPackage(moduleList.pop());
    }
}


prepareJsonPackageFile();

