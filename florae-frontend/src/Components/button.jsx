/**
 * Button is a reusable React component for rendering customizable buttons.
 * Supports styling, click handling, and optional states (like loading or disabled).
 *
 * Props:
 * - children (React.Node): Content to show inside the button.
 * - onClick (function): Handler for button click events.
 * - ...otherProps: Any additional HTML button attributes.
 *
 * Usage:
 * ```
 * <Button onClick={handleSubmit}>Submit</Button>
 * ```
 */

export default function Button({ buttonText, icon, ...props }) {
  return (
    <button {...props} className={`flex items-center ${props.className || ''}`}>
      {icon && <span className="mr-2">{icon}</span>}
      {buttonText}
    </button>
  );
}
