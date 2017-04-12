import React, {
  Component
} from 'react'

import {
    NativeModules,
    PixelRatio,
    ToolbarAndroid,
    processColor
} from 'react-native'

const NativeBadgeAPI = NativeModules.RNToolbarBadgeAndroidModule

class BadgeToolbarAndroid extends Component {
    replaceBadgedImagesWithDrawables(props) {
        Promise.all((props.actions || []).map((action) => {
            if (action.badge && action.icon && action.icon.uri) {
                const {icon, badge: {width = 32, height = 32, backgroundColor = '#f00', textColor = '#fff', textSize = 16, maxNumber = 9, number = 0}} = action

                return new Promise((resolve, reject) => {
                    const pixelRatio = PixelRatio.get()
                    const w = width * pixelRatio
                    const h = height * pixelRatio
                    const bgColor = processColor(backgroundColor)
                    const txtColor = processColor(textColor)
                    const txtSize = textSize * pixelRatio

                    NativeBadgeAPI.obtainBadgeDrawableName(icon, w, h, bgColor, txtColor, txtSize, maxNumber, number, (error, drawableName) => {
                        if (!error && drawableName) {
                            resolve({...action, icon: {uri: drawableName}})
                        } else {
                            reject(typeof error === 'string' ? new Error(error) : error)
                        }
                    })
                })
            }
            return Promise.resolve(action)
        })).then(actions => this.setState({actions}))
    }

    componentWillMount() {
        this.replaceBadgedImagesWithDrawables(this.props)
    }

    componentWillReceiveProps(nextProps) {
        this.replaceBadgedImagesWithDrawables(nextProps)
    }

    render() {
        return <ToolbarAndroid {...this.props} {...this.state} />
    }
}

export default BadgeToolbarAndroid
