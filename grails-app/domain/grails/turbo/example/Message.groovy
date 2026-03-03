package grails.turbo.example

/**
 * Example domain class for Turbo demonstrations.
 */
class Message {
    String title
    String body
    Date dateCreated
    Date lastUpdated

    static constraints = {
        title blank: false, maxSize: 255
        body blank: false, maxSize: 5000
    }

    static mapping = {
        sort dateCreated: 'desc'
    }

    String toString() {
        title
    }
}

