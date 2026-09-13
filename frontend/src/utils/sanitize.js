const ALLOWED_TAGS = new Set([
  'P', 'BR', 'B', 'STRONG', 'I', 'EM', 'U', 'UL', 'OL', 'LI',
  'BLOCKQUOTE', 'CODE', 'PRE', 'A', 'IMG'
])

const ALLOWED_ATTRS = {
  A: new Set(['href', 'target']),
  IMG: new Set(['src', 'width', 'height', 'alt'])
}

const URL_ATTRS = new Set(['href', 'src'])
const ALLOWED_PROTOCOLS = new Set(['http:', 'https:', 'mailto:'])

function sanitizeElement(element) {
  Array.from(element.children).forEach(child => {
    if (!ALLOWED_TAGS.has(child.tagName)) {
      child.replaceWith(document.createTextNode(child.textContent || ''))
      return
    }

    Array.from(child.attributes).forEach(attr => {
      const allowed = ALLOWED_ATTRS[child.tagName]
      const attrName = attr.name.toLowerCase()
      if (!allowed || !allowed.has(attrName)) {
        child.removeAttribute(attr.name)
        return
      }

      if (URL_ATTRS.has(attrName)) {
        const value = attr.value || ''
        const trimmed = value.trim()
        if (trimmed.startsWith('/') || trimmed.startsWith('./') || trimmed.startsWith('../')) {
          return
        }
        try {
          const url = new URL(trimmed, window.location.origin)
          if (!ALLOWED_PROTOCOLS.has(url.protocol)) {
            child.removeAttribute(attr.name)
          }
        } catch (e) {
          child.removeAttribute(attr.name)
        }
      }
    })

    if (child.tagName === 'A' && child.getAttribute('target') === '_blank') {
      child.setAttribute('rel', 'noopener noreferrer')
    }

    sanitizeElement(child)
  })
}

export function sanitizeRichText(value) {
  if (!value) {
    return ''
  }
  if (typeof window === 'undefined' || typeof document === 'undefined') {
    return String(value)
  }
  const wrapper = document.createElement('div')
  wrapper.innerHTML = String(value)
  sanitizeElement(wrapper)
  return wrapper.innerHTML
}

export function stripHtml(value) {
  if (!value) {
    return ''
  }
  if (typeof document === 'undefined') {
    return String(value).replace(/<[^>]+>/g, '')
  }
  const wrapper = document.createElement('div')
  wrapper.innerHTML = String(value)
  return wrapper.textContent || wrapper.innerText || ''
}
