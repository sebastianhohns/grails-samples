package org.grails.auth

import org.apache.shiro.SecurityUtils

/**
* @author Graeme Rocher
* @since 1.0
*
* Created: Feb 26, 2008
*/
class JSecurityAuthFilters {
    
    /**
     * Called when an unauthenticated user tries to access a secured
     * page.
     */
    def onNotAuthenticated(subject, d) {
        def targetUri = d.request.forwardURI 
        if (d.request.queryString) {
            targetUri = "${targetUri}?${d.request.queryString}"
        }

        if (d.request.xhr) {
            d.render(template:"/user/loginForm", model:[originalURI:targetUri,
                                                        formData:d.params,
                                                        async:true,
                                                        update:d.params.update,
                                                        message:"auth.not.logged.in"])
        } else {
            // Redirect to login page.
            d.redirect(
                    controller: 'user',
                    action: 'login',
                    params:[originalURI: targetUri])
        }
    }    

    static filters = {
       // Ensure that all controllers and actions require an authenticated user,
        
        // Creating, modifying, or deleting a book requires the "Administrator"
        // role.
        wikiEditing(controller: "(content|news|plugin)", action: "(editNews|createNews|markupWikiPage|editWikiPage|createWikiPage|saveWikiPage|editPlugin|createPlugin|uploadImage)") {
            before = {
                accessControl {
                    role("Editor") || role("Administrator")
                }
            }
        }
        jobPosting(controller:"(job|paypal)", action:"(delete|edit|update|editJobs|save|create|buy|success|cancel)") {
            before = {
                accessControl {
                    role("Editor") || role("Administrator")
                }
            }
        }
        wikiManagement(controller:"content", action:"rollbackWikiVersion") {
            before = {
                accessControl {
                    role("Administrator")
                }
            }
        }
        userProfile(controller:"user", action:"profile") {
            before = {
                accessControl {
                    role("Editor") || role("Administrator")
                }
            }
        }
        comments(controller:"commentable", action:"add") {
            before = {
                accessControl { true }
            }
        }
        screencasts(controller:"screencast", action:"(edit|create|save|update)") {
            before = {
                accessControl { true }
            }
        }
        blogPosting(controller:"blog", action:"(createEntry|editEntry)") {
            before = {
                accessControl { true }
            }
        }
        blogDeletion(controller:"blog", action:"delete") {
            before = {
                accessControl {
                    role("Administrator")
                }
            }
        }

        pluginDeletion(controller:"plugin", action:"deletePlugin") {
            before = {
                accessControl {
                    role("Administrator")
                }
            }
        }

        pluginActivities(controller:"(tag|plugin|rateable)", action:"(postComment|autoCompleteNames|rate)") {
            before = {
                accessControl {
                    role("Editor") || role("Administrator")
                }
            }
        }

        adminArea(uri:"/admin/**") {
            before = {
                if (controllerName == "error") return true

                accessControl {
                    role("Administrator")
                }
            }
        }

        userInRequest(controller:"*", action:"*") {
            before = {
                if (controllerName == "error") return true

                def subject = SecurityUtils.getSubject()
                if(subject && subject?.principal) {
                    request.user = User.findByLogin(subject.principal, [cache:true])
                }
            }
        }
    }
}
