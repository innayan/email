import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.AutoMerge
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.Swabra
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.merge
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.notifications
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2019.2"

project {

    vcsRoot(HttpsGithubComInnayanBuildoneRefsHeadsMaster)
    vcsRoot(HttpsGithubComInnayanSecku6)

    buildType(FailedToStart)
    buildType(SuccessfulBuild)
    buildType(EmailComposite)
    buildType(FailedBuildBuild)

    template(EmailTemplate)

    params {
        password("wdotjfgkjfgjfkjg", "credentialsJSON:4b7f2800-1421-459e-9999-512194609621")
    }
}

object EmailComposite : BuildType({
    name = "Email Composite"

    type = BuildTypeSettings.Type.COMPOSITE

    vcs {
        showDependenciesChanges = true
    }

    features {
        notifications {
            notifier = "email"
            buildFailed = true
            param("email", "inna.yankelevich@jetbrains.com")
        }
    }

    dependencies {
        snapshot(FailedBuildBuild) {
            reuseBuilds = ReuseBuilds.NO
            onDependencyCancel = FailureAction.ADD_PROBLEM
        }
        snapshot(FailedToStart) {
            reuseBuilds = ReuseBuilds.NO
            onDependencyCancel = FailureAction.ADD_PROBLEM
        }
        snapshot(SuccessfulBuild) {
            reuseBuilds = ReuseBuilds.NO
            onDependencyCancel = FailureAction.ADD_PROBLEM
        }
    }
})

object FailedBuildBuild : BuildType({
    templates(EmailTemplate)
    name = "Failed build"

    features {
        swabra {
            id = "swabra"
            enabled = false
            forceCleanCheckout = true
            lockingProcesses = Swabra.LockingProcessPolicy.KILL
            verbose = true
            paths = "+:remote/move/**"
        }
        merge {
            id = "BUILD_EXT_52"
            branchFilter = """
                111a
                +:refs/heads/123
                +:refs/heads/bbbb
                +:refs/heads/bran2
            """.trimIndent()
            destinationBranch = "<default>111a"
            commitMessage = "Merge branch '%teamcity.build.branch%'111"
            mergePolicy = AutoMerge.MergePolicy.FAST_FORWARD
            mergeCondition = "noNewTests"
        }
        notifications {
            id = "BUILD_EXT_1"
            notifier = "email"
            brachFilter = "+:refs/heads/123"
            buildStarted = true
            buildFailed = true
            buildFinishedSuccessfully = true
            param("email", "inna_yan@mail.ru")
        }
        commitStatusPublisher {
            id = "BUILD_EXT_51"
            vcsRootExtId = "GitJavaEclipse"
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "credentialsJSON:3167a025-8dc7-4fbe-b293-6677c11df3d0"
                }
            }
        }
        notifications {
            id = "BUILD_EXT_56"
            notifier = "email"
            brachFilter = "+:refs/heads/bbbb"
            buildStarted = false
            buildFailedToStart = false
            buildFailed = false
            firstFailureAfterSuccess = false
            newBuildProblemOccured = false
            buildFinishedSuccessfully = false
            firstBuildErrorOccurs = false
            buildProbablyHanging = false
            investigationUpdated = false
            muteUpdated = false
            param("email", "inna_yan@mail.ru")
        }
        feature {
            id = "BUILD_EXT_53"
            type = "xml-report-plugin"
            param("xmlReportParsing.reportType", "ctest")
            param("xmlReportParsing.reportDirs", "ddd")
        }
    }

    cleanup {
        keepRule {
            id = "KEEP_RULE_12"
            keepAtLeast = allBuilds()
            applyToBuilds {
                withTags = anyOf("tag")
            }
            dataToKeep = historyAndStatistics {
                preserveArtifacts = all()
            }
            applyPerEachBranch = true
            preserveArtifactsDependencies = true
        }
    }
    
    disableSettings("BUILD_EXT_2", "RUNNER_1")
})

object FailedToStart : BuildType({
    name = "Failed to start"

    vcs {
        root(HttpsGithubComInnayanBuildoneRefsHeadsMaster)
    }

    triggers {
        vcs {
        }
    }
})

object SuccessfulBuild : BuildType({
    name = "Successful build"

    steps {
        script {
            scriptContent = "echo email"
        }
    }

    features {
        notifications {
            notifier = "email"
            buildFinishedSuccessfully = true
            param("email", "inna_yan@mail.ru")
        }
        notifications {
            notifier = "email"
            buildFailed = true
            param("email", "inna_yan@mail.ru")
        }
    }
})

object EmailTemplate : Template({
    name = "Email template"

    vcs {
        root(AbsoluteId("GitJavaEclipse"))
    }

    steps {
        script {
            id = "RUNNER_1"
            scriptContent = "echo email"
        }
        maven {
            id = "RUNNER_2"
            goals = "clean test"
            pomLocation = "java_eclipse/pom.xml"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
    }

    features {
        notifications {
            id = "BUILD_EXT_1"
            notifier = "email"
            brachFilter = "+:*"
            firstBuildErrorOccurs = true
            param("email", "test@mail.ru")
        }
        notifications {
            id = "BUILD_EXT_2"
            enabled = false
            notifier = "email"
            brachFilter = """
                +:<default>
                +:refs/heads/123
            """.trimIndent()
            buildFailed = true
            param("email", "inna_yan@mail.ru")
        }
        merge {
            id = "BUILD_EXT_52"
            branchFilter = "111"
            destinationBranch = "<default>111"
            commitMessage = "Merge branch '%teamcity.build.branch%'111"
            mergePolicy = AutoMerge.MergePolicy.FAST_FORWARD
            mergeCondition = "noNewTests"
        }
    }
})

object HttpsGithubComInnayanBuildoneRefsHeadsMaster : GitVcsRoot({
    name = "https://github.com/innayan/buildone#refs/heads/master"
    url = "https://github.com/innayan/buildone1"
    authMethod = password {
        userName = "innayan"
        password = "credentialsJSON:fffda5e5-0935-454b-b399-080161d403bc"
    }
})

object HttpsGithubComInnayanSecku6 : GitVcsRoot({
    name = "https://github.com/innayan/secku6"
    url = "https://github.com/innayan/secku6"
    authMethod = password {
        userName = "innayan"
        password = "credentialsJSON:7d09fc8e-75f2-4c22-8ac4-e2bd1f007480"
    }
})
